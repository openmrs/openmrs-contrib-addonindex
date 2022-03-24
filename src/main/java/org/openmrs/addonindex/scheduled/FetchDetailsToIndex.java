/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.scheduled;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.util.ListIterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.backend.SupportsDownloadCounts;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.openmrs.addonindex.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * For each of the Add-Ons we're supposed to index, uses its backend handler to fetch details and available versions
 */
@Component
@Slf4j
public class FetchDetailsToIndex {
	
	private final IndexingService indexingService;
	
	private final RestTemplateBuilder restTemplateBuilder;
	
	private final DocumentBuilderFactory documentBuilderFactory;
	
	@Autowired
	public FetchDetailsToIndex(IndexingService indexingService,
			RestTemplateBuilder restTemplateBuilder) {
		this.indexingService = indexingService;
		this.restTemplateBuilder = restTemplateBuilder;
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setValidating(false);
	}
	
	@Value("${scheduler.fetch_details_to_index.fetch_extra_details}")
	private boolean fetchExtraDetails = true;
	
	@Scheduled(
			initialDelayString = "${scheduler.fetch_details_to_index.initial_delay}",
			fixedDelayString = "${scheduler.fetch_details_to_index.period}")
	public void run() {
		AllAddOnsToIndex allToIndex = indexingService.getAllToIndex();
		log.info("Fetching details for {} add-ons", allToIndex.size());
		for (AddOnToIndex toIndex : allToIndex.getToIndex()) {
			log.debug("Running scheduled index for {}", toIndex.getUid());
			try {
				getDetailsAndIndex(toIndex);
			} catch (Exception e) {
				log.error("Error getting details for {}", toIndex.getUid(), e);
			}
		}
	}
	
	void setFetchExtraDetails(boolean fetchExtraDetails) {
		this.fetchExtraDetails = fetchExtraDetails;
	}
	
	void getDetailsAndIndex(AddOnToIndex toIndex) {
		indexingService.getIndexingStatus().setStatus(toIndex, IndexingStatus.Status.indexingNow());
		try {
			BackendHandler handler = indexingService.getHandlerFor(toIndex);
			AddOnInfoAndVersions infoAndVersions = handler.getInfoAndVersionsFor(toIndex);
			if (log.isDebugEnabled()) {
				log.debug("{} has {} versions", toIndex.getUid(), infoAndVersions.getVersions().size());
			}
			
			if (fetchExtraDetails) {
				if (handler instanceof SupportsDownloadCounts) {
					((SupportsDownloadCounts) handler).fetchDownloadCounts(toIndex, infoAndVersions);
				}
				fetchExtraDetailsForEachVersion(toIndex, infoAndVersions);
			}
			
			infoAndVersions.setDetailsBasedOnLatestVersion();
			indexingService.index(infoAndVersions);
			indexingService.getIndexingStatus().setStatus(toIndex,
					IndexingStatus.Status.success(new AddOnInfoSummary(infoAndVersions)));
		}
		catch (Exception ex) {
			log.error("Error indexing {}", toIndex.getUid(), ex);
			indexingService.getIndexingStatus().setStatus(toIndex, IndexingStatus.Status.error(ex));
		}
	}
	
	void fetchExtraDetailsForEachVersion(AddOnToIndex toIndex, AddOnInfoAndVersions infoAndVersions) throws Exception {
		AddOnInfoAndVersions existingInfo = indexingService.getByUid(toIndex.getUid());
		
		for (ListIterator<AddOnVersion> iter = infoAndVersions.getVersions().listIterator(); iter.hasNext(); ) {
			AddOnVersion version = iter.next();
			if (existingInfo != null) {
				Optional<AddOnVersion> existingVersion = existingInfo.getVersion(version.getVersion());
				if (existingVersion.isPresent() &&
						existingVersion.get().getDownloadUri().equals(version.getDownloadUri()) &&
						(version.getReleaseDatetime() == null ||
								version.getReleaseDatetime().equals(existingVersion.get().getReleaseDatetime()))) {
					
					log.debug("Using existing data for {} versions {}", toIndex.getUid(), version.getVersion());
					iter.set(existingVersion.get());
					continue;
				}
			}
			
			try {
				if (toIndex.getType() == AddOnType.OMOD) {
					log.info("Fetching OMOD for {} {}", toIndex.getUid(), version.getVersion());
					String configXml = fetchConfigXml(version);
					if (configXml == null) {
						throw new IllegalArgumentException("No config.xml file in " + version.getDownloadUri());
					} else {
						handleConfigXml(configXml, version);
					}
				}
			}
			catch (Exception ex) {
				log.warn("Error fetching/parsing details of {}:{}", toIndex.getUid(), version.getVersion(), ex);
				// don't fail here, keep going
			}
		}
	}
	
	String fetchConfigXml(AddOnVersion addOnVersion) throws IOException {
		log.info("fetching config.xml from {}", addOnVersion.getDownloadUri());
		Resource resource = restTemplateBuilder.build().getForObject(addOnVersion.getDownloadUri(), Resource.class);
		if (resource != null) {
			try (
					ZipInputStream zis = new ZipInputStream(new BufferedInputStream(resource.getInputStream()))
			) {
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.getName().equals("config.xml")) {
						if (addOnVersion.getReleaseDatetime() == null) {
							addOnVersion.setReleaseDatetime(entry.getCreationTime().toInstant().atOffset(ZoneOffset.UTC));
						}
						
						return StreamUtils.copyToString(zis, Charset.defaultCharset());
					}
				}
			}
		}
		
		return null;
	}
	
	void handleConfigXml(String configXml, AddOnVersion addOnVersion) throws Exception {
		// sometimes this says something like <!DOCTYPE ... "../lib-common/config-1.0.dtd">
		// we don't need DTD validation in any case, so we strip any DOCTYPE
		configXml = configXml.replaceAll("(?s)<!DOCTYPE .*?>", "");
		XPath xpath = XPathFactory.newInstance().newXPath();
		Document config = documentBuilderFactory.newDocumentBuilder().parse(
				new InputSource(new StringReader(configXml)));
		handleRequireOpenmrsVersion(addOnVersion, xpath, config);
		handleRequireModules(addOnVersion, xpath, config);
		handleSupportedLanguages(addOnVersion, xpath, config);
		handleModuleIdAndPackage(addOnVersion, xpath, config);
		
	}
	
	private void handleSupportedLanguages(AddOnVersion addOnVersion, XPath xpath, Document config)
			throws XPathExpressionException {
		NodeList nodeList = (NodeList) xpath.evaluate("/module/messages/lang", config, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node item = nodeList.item(i);
			addOnVersion.addLanguage(item.getTextContent().trim());
		}
	}
	
	private void handleRequireModules(AddOnVersion addOnVersion, XPath xpath, Document config)
			throws XPathExpressionException {
		NodeList nodeList = (NodeList) xpath.evaluate("/module/require_modules/require_module", config,
				XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node item = nodeList.item(i);
			Node version = item.getAttributes().getNamedItem("version");
			String requiredModule = item.getTextContent().trim();
			String requiredVersion = version == null ? null : version.getTextContent().trim();
			// sometimes modules are inadvertently uploaded without substituting maven variables in config.xml and we end
			// up with a required module version like ${reportingVersion}
			if (requiredVersion != null && requiredVersion.startsWith("${")) {
				requiredVersion = null;
			}
			addOnVersion.addRequiredModule(requiredModule, requiredVersion);
		}
	}
	
	private void handleRequireOpenmrsVersion(AddOnVersion addOnVersion, XPath xpath, Document config)
			throws XPathExpressionException {
		Object str = xpath.evaluate("/module/require_version/text()", config, XPathConstants.STRING);
		if (StringUtils.hasText((String) str)) {
			addOnVersion.setRequireOpenmrsVersion(((String) str).trim());
		}
	}
	
	private void handleModuleIdAndPackage(AddOnVersion addOnVersion, XPath xpath,
			Document config) throws XPathExpressionException {
		Object modulePackage = xpath.evaluate("/module/package/text()", config, XPathConstants.STRING);
		Object moduleId = xpath.evaluate("/module/id/text()", config, XPathConstants.STRING);
		if (StringUtils.hasText((String) modulePackage)) {
			addOnVersion.setModulePackage(((String) modulePackage).trim());
		}
		if (StringUtils.hasText((String) moduleId)) {
			addOnVersion.setModuleId(((String) moduleId).trim());
		}
	}
}
