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

import java.io.StringReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;

/**
 * Fetches versions for OpenMRS content packages published as Maven .zip artifacts under
 * org.openmrs.content in the OpenMRS Nexus public repository. Reads maven-metadata.xml and indexes
 * release versions only (SNAPSHOTs are skipped). Per-version requirements are read later by
 * FetchDetailsToIndex from content.properties inside the zip.
 */
@Component
@Slf4j
public class MavenContentPackage implements BackendHandler {
	
	public static final String PUBLIC_REPO_URL = "https://mavenrepo.openmrs.org/nexus/content/repositories/public/";
	
	private final RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	public MavenContentPackage(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String url = artifactBaseUrl(addOnToIndex) + "maven-metadata.xml";
		log.info("Getting info from {}", url);
		String xml = restTemplateBuilder.build().getForObject(url, String.class);
		return handleMavenMetadata(addOnToIndex, xml);
	}
	
	AddOnInfoAndVersions handleMavenMetadata(AddOnToIndex addOnToIndex, String xml) throws Exception {
		AddOnInfoAndVersions info = AddOnInfoAndVersions.from(addOnToIndex);
		String base = artifactBaseUrl(addOnToIndex);
		String artifactId = addOnToIndex.getMavenRepoDetails().getArtifactId();
		info.setHostedUrl(base);
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xpath.evaluate("//versioning/versions/version", new InputSource(new StringReader(xml)),
		    XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i) {
			String versionString = nodes.item(i).getTextContent().trim();
			if (versionString.endsWith("-SNAPSHOT")) {
				continue;
			}
			AddOnVersion version = new AddOnVersion();
			version.setVersion(new Version(versionString));
			version.setDownloadUri(base + versionString + "/" + artifactId + "-" + versionString + ".zip");
			info.addVersion(version);
		}
		return info;
	}
	
	private String artifactBaseUrl(AddOnToIndex addOnToIndex) {
		MavenRepoDetails details = addOnToIndex.getMavenRepoDetails();
		return PUBLIC_REPO_URL + details.getGroupId().replace('.', '/') + "/" + details.getArtifactId() + "/";
	}
}
