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
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IndexingService indexingService;
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
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
	
	private void getDetailsAndIndex(AddOnToIndex toIndex) throws Exception {
		BackendHandler handler = indexingService.getHandlerFor(toIndex);
		AddOnInfoAndVersions infoAndVersions = handler.getInfoAndVersionsFor(toIndex);
		if (logger.isDebugEnabled()) {
			logger.debug(toIndex.getUid() + " has " + infoAndVersions.getVersions().size() + " versions");
		}
		
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
		
		indexingService.index(infoAndVersions);
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
					String configXml = StreamUtils.copyToString(zis, Charset.defaultCharset());
					return configXml;
				}
			}
		}
		return null;
	}
	
	void handleConfigXml(String configXml, AddOnVersion addOnVersion) throws Exception {
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
			addOnVersion.addRequiredModule(requiredModule, version == null ? null : version.getTextContent().trim());
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
