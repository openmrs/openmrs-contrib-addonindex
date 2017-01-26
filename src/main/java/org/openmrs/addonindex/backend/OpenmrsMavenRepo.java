package org.openmrs.addonindex.backend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * OpenMRS has historically released many of its modules in its maven repository (basically, all the modules in the
 * Reference Application, or in PIH's EMR, or that are built by ci.openmrs.org are released this way).
 *
 * Usually the artifacts are deployed to maven with type=jar and named like "{moduleId}-omod-{version}.jar" so they need
 * to be renamed before being deployed to OpenMRS.
 *
 * This Backend is a bit of a hack, but it allows us to rapidly start using this application, even before we've shifed
 * many of our releases to Bintray or Github Releases.
 */
@Component
public class OpenmrsMavenRepo implements BackendHandler {
	
	private String BASE_URL = "http://mavenrepo.openmrs.org/nexus/";
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	private DocumentBuilderFactory factory;
	
	private Pattern renamePattern = Pattern.compile("(.+)-omod-([0-9.]+).jar");
	
	public OpenmrsMavenRepo() throws Exception {
		factory = DocumentBuilderFactory.newInstance();
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String url = BASE_URL + "service/local/repositories/modules/index_content/"
				+ "?groupIdHint=" + addOnToIndex.getMavenRepoDetails().getGroupId()
				+ "&artifactIdHint=" + addOnToIndex.getMavenRepoDetails().getArtifactId();
		String xml = restTemplateBuilder.build().getForObject(url, String.class);
		return handleIndexBrowserTreeViewResponse(addOnToIndex, xml);
	}
	
	AddOnInfoAndVersions handleIndexBrowserTreeViewResponse(AddOnToIndex addOnToIndex, String xml) throws Exception {
		AddOnInfoAndVersions addOnInfoAndVersions = AddOnInfoAndVersions.from(addOnToIndex);
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList result = (NodeList) xpath.evaluate("//indexBrowserTreeNode[type='artifact' and not(classifier)]",
				new InputSource(new StringReader(xml)),
				XPathConstants.NODESET);
		
		for (int i = 0; i < result.getLength(); ++i) {
			Element node = (Element) result.item(i);
			String path = node.getElementsByTagName("path").item(0).getTextContent();
			String version = node.getElementsByTagName("version").item(0).getTextContent();
			
			String nodeName = node.getElementsByTagName("nodeName").item(0).getTextContent();
			// typically this will be like "appui-omod-1.6.jar"
			String renameTo = null;
			Matcher matcher = renamePattern.matcher(nodeName);
			if (matcher.matches()) {
				renameTo = matcher.group(1) + "-" + matcher.group(2) + ".omod";
			}
			
			AddOnVersion addOnVersion = new AddOnVersion();
			addOnVersion.setVersion(new Version(version));
			addOnVersion.setDownloadUri(BASE_URL + "service/local/repositories/modules/content" + path);
			addOnVersion.setRenameTo(renameTo);
			try {
				if (addOnToIndex.getType() == AddOnType.OMOD) {
					String configXml = fetchConfigXml(addOnToIndex, addOnVersion);
					if (configXml == null) {
						throw new IllegalArgumentException("No config.xml file in " + addOnVersion.getDownloadUri());
					} else {
						handleConfigXml(configXml, addOnVersion);
					}
				}
			}
			catch (Exception ex) {
				logger.warn("Error fetching/parsing details of " + addOnToIndex.getUid() + ":" + addOnVersion.getVersion()
						, ex);
				// don't fail here, keep going
			}
			addOnInfoAndVersions.addVersion(addOnVersion);
		}
		
		return addOnInfoAndVersions;
	}
	
	void handleConfigXml(String configXml, AddOnVersion addOnVersion) throws Exception {
		XPath xpath = XPathFactory.newInstance().newXPath();
		Document config = factory.newDocumentBuilder().parse(
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
			String requiredModule = item.getTextContent();
			addOnVersion.addRequiredModule(requiredModule, version == null ? null : version.getTextContent());
		}
	}
	
	private void handleRequireOpenmrsVersion(AddOnVersion addOnVersion, XPath xpath, Document config)
			throws XPathExpressionException {
		Object str = xpath.evaluate("/module/require_version/text()", config, XPathConstants.STRING);
		if (StringUtils.hasText((String) str)) {
			addOnVersion.setRequireOpenmrsVersion((String) str);
		}
	}
	
	String fetchConfigXml(AddOnToIndex addOnToIndex, AddOnVersion addOnVersion) throws IOException {
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
}
