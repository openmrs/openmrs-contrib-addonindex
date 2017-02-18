package org.openmrs.addonindex.scheduled;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ListIterator;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.openmrs.addonindex.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FetchDetailsToIndex {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private IndexingService indexingService;
	
	private RestTemplateBuilder restTemplateBuilder;
	
	private DocumentBuilderFactory documentBuilderFactory;
	
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
		logger.info("Fetching details for " + allToIndex.size() + " add-ons");
		for (AddOnToIndex toIndex : allToIndex.getToIndex()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Running scheduled index for " + toIndex.getUid());
			}
			try {
				getDetailsAndIndex(toIndex);
			}
			catch (Exception e) {
				logger.error("Error getting details for " + toIndex.getUid(), e);
			}
		}
	}
	
	void setFetchExtraDetails(boolean fetchExtraDetails) {
		this.fetchExtraDetails = fetchExtraDetails;
	}
	
	void getDetailsAndIndex(AddOnToIndex toIndex) throws Exception {
		indexingService.getIndexingStatus().setStatus(toIndex, IndexingStatus.Status.indexingNow());
		try {
			BackendHandler handler = indexingService.getHandlerFor(toIndex);
			AddOnInfoAndVersions infoAndVersions = handler.getInfoAndVersionsFor(toIndex);
			if (logger.isDebugEnabled()) {
				logger.debug(toIndex.getUid() + " has " + infoAndVersions.getVersions().size() + " versions");
			}
			
			if (fetchExtraDetails) {
				fetchExtraDetailsForEachVersion(toIndex, infoAndVersions);
			}
			
			indexingService.index(infoAndVersions);
			indexingService.getIndexingStatus().setStatus(toIndex,
					IndexingStatus.Status.success(new AddOnInfoSummary(infoAndVersions)));
		}
		catch (Exception ex) {
			logger.error("Error indexing " + toIndex.getUid(), ex);
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
						existingVersion.get().getDownloadUri().equals(version.getDownloadUri())) {
					if (logger.isDebugEnabled()) {
						logger.debug("Using existing data for " + toIndex.getUid() + " version " + version.getVersion());
					}
					iter.set(existingVersion.get());
					continue;
				}
			}
			try {
				if (toIndex.getType() == AddOnType.OMOD) {
					logger.info("Fetching OMOD for " + toIndex.getUid() + " " + version.getVersion());
					String configXml = fetchConfigXml(toIndex, version);
					if (configXml == null) {
						throw new IllegalArgumentException("No config.xml file in " + version.getDownloadUri());
					} else {
						handleConfigXml(configXml, version);
					}
				}
			}
			catch (Exception ex) {
				logger.warn("Error fetching/parsing details of " + toIndex.getUid() + ":" + version.getVersion(), ex);
				// don't fail here, keep going
			}
		}
	}
	
	String fetchConfigXml(AddOnToIndex addOnToIndex, AddOnVersion addOnVersion) throws IOException {
		logger.info("fetching config.xml from " + addOnVersion.getDownloadUri());
		Resource resource = restTemplateBuilder.build().getForObject(addOnVersion.getDownloadUri(), Resource.class);
		try (
				InputStream inputStream = resource.getInputStream();
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream))
		) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals("config.xml")) {
					return StreamUtils.copyToString(zis, Charset.defaultCharset());
				}
			}
		}
		return null;
	}
	
	void handleConfigXml(String configXml, AddOnVersion addOnVersion) throws Exception {
		// sometimes this says something like <!DOCTYPE ... "../lib-common/config-1.0.dtd">
		// we don't need DTD validation in any case
		configXml = configXml.replaceAll("<!DOCTYPE .*?>", "");
		XPath xpath = XPathFactory.newInstance().newXPath();
		Document config = documentBuilderFactory.newDocumentBuilder().parse(
				new InputSource(new StringReader(configXml)));
		handleRequireOpenmrsVersion(addOnVersion, xpath, config);
		handleRequireModules(addOnVersion, xpath, config);
		handleSupportedLanguages(addOnVersion, xpath, config);
	}
	
	private void handleSupportedLanguages(AddOnVersion addOnVersion, XPath xpath, Document config)
			throws XPathExpressionException {
		NodeList nodeList = (NodeList) xpath.evaluate("/module/messages/lang", config, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node item = nodeList.item(i);
			addOnVersion.addLanguage(item.getTextContent());
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
			addOnVersion.setRequireOpenmrsVersion((String) str);
		}
	}
}
