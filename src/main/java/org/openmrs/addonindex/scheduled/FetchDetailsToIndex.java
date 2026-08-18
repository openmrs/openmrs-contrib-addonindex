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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.backend.SupportsDownloadCounts;
import org.openmrs.addonindex.backend.SupportsVersionDetails;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.VersionRangeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;

/**
 * For each of the Add-Ons we're supposed to index, uses its backend handler to fetch details and
 * available versions
 */
@Component
@Slf4j
public class FetchDetailsToIndex {
	
	private static final String SPA_PREFIX = "spa.frontendModules.";
	
	/**
	 * The groupId each content.properties namespace is published under, used when a dependency doesn't
	 * declare one of its own
	 */
	private static final Map<String, String> DEFAULT_GROUP_IDS = Map.of("omod", "org.openmrs.module", "owa",
	    "org.openmrs.owa", "content", "org.openmrs.content");
	
	private final IndexingService indexingService;
	
	private final RestTemplateBuilder restTemplateBuilder;
	
	private final DocumentBuilderFactory documentBuilderFactory;
	
	@Autowired
	public FetchDetailsToIndex(IndexingService indexingService, RestTemplateBuilder restTemplateBuilder) {
		this.indexingService = indexingService;
		this.restTemplateBuilder = restTemplateBuilder;
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
		this.documentBuilderFactory.setValidating(false);
	}
	
	@Value("${scheduler.fetch_details_to_index.fetch_extra_details}")
	private boolean fetchExtraDetails = true;
	
	@Scheduled(initialDelayString = "${scheduler.fetch_details_to_index.initial_delay}", fixedDelayString = "${scheduler.fetch_details_to_index.period}")
	public void run() {
		AllAddOnsToIndex allToIndex = indexingService.getAllToIndex();
		log.info("Fetching details for {} add-ons", allToIndex.size());
		allToIndex.getToIndex().parallelStream().forEach(toIndex -> {
			log.debug("Running scheduled index for {}", toIndex.getUid());
			try {
				getDetailsAndIndex(toIndex);
			}
			catch (Exception e) {
				log.error("Error getting details for {}", toIndex.getUid(), e);
			}
		});
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
				fetchExtraDetailsForEachVersion(handler, toIndex, infoAndVersions);
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
	
	void fetchExtraDetailsForEachVersion(BackendHandler handler, AddOnToIndex toIndex, AddOnInfoAndVersions infoAndVersions)
	        throws Exception {
		AddOnInfoAndVersions existingInfo = indexingService.getByUid(toIndex.getUid());
		boolean hadVersions = !infoAndVersions.getVersions().isEmpty();
		
		for (ListIterator<AddOnVersion> iter = infoAndVersions.getVersions().listIterator(); iter.hasNext();) {
			AddOnVersion version = iter.next();
			if (existingInfo != null) {
				Optional<AddOnVersion> existingVersion = existingInfo.getVersion(version.getVersion());
				if (existingVersion.isPresent() && existingVersion.get().getDownloadUri().equals(version.getDownloadUri())
				        && (version.getReleaseDatetime() == null
				                || version.getReleaseDatetime().equals(existingVersion.get().getReleaseDatetime()))) {
					
					log.debug("Using existing data for {} versions {}", toIndex.getUid(), version.getVersion());
					iter.set(existingVersion.get());
					continue;
				}
			}
			
			try {
				if (handler instanceof SupportsVersionDetails) {
					((SupportsVersionDetails) handler).fetchVersionDetails(toIndex, version);
				} else if (toIndex.getType() == AddOnType.OMOD) {
					log.info("Fetching OMOD for {} {}", toIndex.getUid(), version.getVersion());
					String configXml = fetchZipEntry(version, "config.xml");
					if (configXml == null) {
						throw new IllegalArgumentException("No config.xml file in " + version.getDownloadUri());
					} else {
						handleConfigXml(configXml, version);
					}
				} else if (toIndex.getType() == AddOnType.CONTENT_PACKAGE) {
					log.info("Fetching content package for {} {}", toIndex.getUid(), version.getVersion());
					String contentProperties = fetchZipEntry(version, "content.properties");
					if (contentProperties == null) {
						// a content package without a manifest is unusual but not fatal; index it anyway
						log.warn("No content.properties file in {}", version.getDownloadUri());
					} else {
						handleContentProperties(contentProperties, version);
					}
				}
			}
			catch (Exception ex) {
				log.warn("Error fetching/parsing details of {}:{}", toIndex.getUid(), version.getVersion(), ex);
				if (handler instanceof SupportsVersionDetails) {
					// the reuse check above compares fields that never change for these versions, so
					// indexing this one without its details would be permanent. Leaving it out of this
					// run means the next run will not find it in the index and will fetch it again.
					// (A permanently unfetchable version retries on every run, which we prefer over
					// indexing wrong data.)
					iter.remove();
				}
				// don't fail here, keep going
			}
		}
		
		if (hadVersions && infoAndVersions.getVersions().isEmpty()) {
			// indexing this would replace the whole document with an empty one and report success
			throw new IllegalStateException(
			        "Every version of " + toIndex.getUid() + " failed its details fetch; not indexing an empty document");
		}
	}
	
	String fetchZipEntry(AddOnVersion addOnVersion, String entryName) throws IOException {
		log.info("fetching {} from {}", entryName, addOnVersion.getDownloadUri());
		ResponseEntity<Resource> response = restTemplateBuilder.build().getForEntity(addOnVersion.getDownloadUri(),
		    Resource.class);
		Resource resource = response.getBody();
		if (resource != null) {
			try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(resource.getInputStream()))) {
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					if (entry.getName().equals(entryName)) {
						if (addOnVersion.getReleaseDatetime() == null) {
							if (entry.getCreationTime() != null) {
								addOnVersion
								        .setReleaseDatetime(entry.getCreationTime().toInstant().atOffset(ZoneOffset.UTC));
							} else if (entry.getLastModifiedTime() != null) {
								addOnVersion.setReleaseDatetime(
								    entry.getLastModifiedTime().toInstant().atOffset(ZoneOffset.UTC));
							} else if (response.getHeaders().containsKey(HttpHeaders.LAST_MODIFIED)) {
								ZonedDateTime zdt = response.getHeaders().getFirstZonedDateTime(HttpHeaders.LAST_MODIFIED);
								if (zdt != null) {
									addOnVersion.setReleaseDatetime(OffsetDateTime.from(zdt));
								}
							}
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
		Document config = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(configXml)));
		handleRequireOpenmrsVersion(addOnVersion, xpath, config);
		handleRequireModules(addOnVersion, xpath, config);
		handleSupportedLanguages(addOnVersion, xpath, config);
		handleModuleIdAndPackage(addOnVersion, xpath, config);
		
	}
	
	void handleContentProperties(String contentProperties, AddOnVersion addOnVersion) throws IOException {
		Properties properties = new Properties();
		properties.load(new StringReader(contentProperties));
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key).trim();
			// Skip keys that aren't dependencies: name/version describe the package itself, *.groupId and
			// *.type are sub-attributes of a dependency (read alongside it below, rather than indexed in
			// their own right), and var.* are configuration values the package exposes for overriding.
			if (key.equals("name") || key.equals("version") || key.endsWith(".groupId") || key.endsWith(".type")
			        || key.startsWith("var.")) {
				continue;
			}
			// Requirements are SemVer ranges here, but strict minimums and wildcards everywhere else we
			// index, so they have to be translated before anything compares them. As in config.xml, a
			// requirement whose version we can't make sense of is still worth recording without one.
			String requiredVersion = null;
			if (value.startsWith("${")) {
				// published without substituting maven variables, e.g. ${openmrsPlatformVersion}
				log.debug("Unsubstituted maven variable {} as the version of {}", value, key);
			} else {
				requiredVersion = VersionRangeConverter.toOpenmrsVersionRange(value);
				if (requiredVersion == null) {
					log.warn("Cannot express version range \"{}\" of {} as an OpenMRS version range", value, key);
				}
			}
			if (key.equals("war.openmrs")) {
				addOnVersion.setRequireOpenmrsVersion(requiredVersion);
			} else if (key.startsWith(SPA_PREFIX)) {
				// frontend modules are npm packages, so the package name is the only identifier they have
				addOnVersion.addRequiredModule(key.substring(SPA_PREFIX.length()), requiredVersion);
			} else {
				addRequiredMavenModule(addOnVersion, properties, key, requiredVersion);
			}
		}
	}
	
	/**
	 * Records an omod/owa/content requirement under the same identifier we index the required add-on
	 * itself by: its Maven groupId and artifactId, which is the module package for an OMOD and the uid
	 * for an OWA or content package. Content packages only declare a groupId when it isn't the usual
	 * one for that namespace (e.g. the event module, which is published under org.openmrs).
	 */
	private void addRequiredMavenModule(AddOnVersion addOnVersion, Properties properties, String key,
	        String requiredVersion) {
		int firstDot = key.indexOf('.');
		String defaultGroupId = firstDot < 0 ? null : DEFAULT_GROUP_IDS.get(key.substring(0, firstDot));
		if (defaultGroupId == null) {
			log.warn("Ignoring content.properties key {}, which is not in a namespace we know how to index", key);
			return;
		}
		String groupId = properties.getProperty(key + ".groupId", defaultGroupId).trim();
		if (groupId.isEmpty() || groupId.startsWith("${")) {
			groupId = defaultGroupId;
		}
		addOnVersion.addRequiredModule(groupId + "." + key.substring(firstDot + 1), requiredVersion);
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
	
	private void handleModuleIdAndPackage(AddOnVersion addOnVersion, XPath xpath, Document config)
	        throws XPathExpressionException {
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
