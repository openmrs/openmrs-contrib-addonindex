/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.backend;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
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
@Slf4j
public class OpenmrsMavenRepo implements BackendHandler {
	
	private static final String BASE_URL = "http://mavenrepo.openmrs.org/nexus/";

	private static final Pattern RENAME_PATTERN = Pattern.compile("(.+)-omod-([0-9.]+).jar");

	private final RestTemplateBuilder restTemplateBuilder;

	@Autowired
	public OpenmrsMavenRepo(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String url = BASE_URL + "service/local/repositories/modules/index_content/"
				+ "?groupIdHint=" + addOnToIndex.getMavenRepoDetails().getGroupId()
				+ "&artifactIdHint=" + addOnToIndex.getMavenRepoDetails().getArtifactId();
		log.info("Getting info from {}", url);
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
			Matcher matcher = RENAME_PATTERN.matcher(nodeName);
			if (matcher.matches()) {
				renameTo = matcher.group(1) + "-" + matcher.group(2) + ".omod";
			}
			
			AddOnVersion addOnVersion = new AddOnVersion();
			addOnVersion.setVersion(new Version(version));
			addOnVersion.setDownloadUri(BASE_URL + "service/local/repositories/modules/content" + path);
			addOnVersion.setRenameTo(renameTo);
			
			addOnInfoAndVersions.addVersion(addOnVersion);
		}
		
		return addOnInfoAndVersions;
	}
}
