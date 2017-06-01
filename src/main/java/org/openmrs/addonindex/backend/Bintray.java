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

import java.io.IOException;
import java.time.OffsetDateTime;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class Bintray implements BackendHandler {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private RestTemplateBuilder restTemplateBuilder;
	
	@Value("${bintray.username}")
	private String bintrayUsername;
	
	@Value("${bintray.api_key}")
	private String bintrayApiKey;
	
	@Autowired
	public Bintray(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		if (StringUtils.isEmpty(bintrayUsername) || StringUtils.isEmpty(bintrayApiKey)) {
			logger.error("You need to specify the bintray.username and bintray.api_key configuration settings");
		}
		String url = packageUrlFor(addOnToIndex);
		ResponseEntity<String> entity = restTemplateBuilder.basicAuthorization(bintrayUsername, bintrayApiKey).build()
				.getForEntity(url, String.class);
		if (!entity.getStatusCode().is2xxSuccessful()) {
			logger.warn("Problem fetching " + url + " -> " + entity.getStatusCode() + " " + entity.getBody());
			return null;
		} else {
			String json = entity.getBody();
			return handlePackageJson(addOnToIndex, json);
		}
	}
	
	AddOnInfoAndVersions handlePackageJson(AddOnToIndex addOnToIndex, String packageJson) throws IOException {
		AddOnInfoAndVersions info = AddOnInfoAndVersions.from(addOnToIndex);
		info.setHostedUrl(hostedUrlFor(addOnToIndex));
		ObjectNode obj = new ObjectMapper().readValue(packageJson, ObjectNode.class);
		if (StringUtils.isEmpty(info.getName())) {
			info.setName(obj.get("name").asText());
		}
		if (StringUtils.isEmpty(info.getDescription())) {
			info.setDescription(obj.get("desc").asText());
		}
		String expectedFileExtension = "." + info.getType().getFileExtension();
		for (JsonNode node : obj.path("versions")) {
			String versionString = node.asText();
			// TODO do we need to GET the version and make sure it's published?
			ArrayNode arr = restTemplateBuilder.basicAuthorization(bintrayUsername, bintrayApiKey).build()
					.getForObject(getVersionFilesUrlFor(addOnToIndex, versionString), ArrayNode.class);
			for (JsonNode fileNode : arr) {
				if (fileNode.get("name").asText().endsWith(expectedFileExtension)) {
					// found the type of file we want, so assume this is the right file.
					// TODO maybe test that it has the version number in it?
					AddOnVersion version = new AddOnVersion();
					version.setVersion(new Version(versionString));
					version.setReleaseDatetime(OffsetDateTime.parse(fileNode.get("created").asText()));
					version.setDownloadUri(downloadUriFor(addOnToIndex, fileNode.get("path").asText()));
					info.addVersion(version);
					break;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping file: " + arr.get("name").asText());
				}
			}
		}
		return info;
	}
	
	private String downloadUriFor(AddOnToIndex addOnToIndex, String filePath) {
		BintrayPackageDetails details = addOnToIndex.getBintrayPackageDetails();
		return String.format("https://bintray.com/%s/%s/download_file?file_path=%s",
				details.getOwner(),
				details.getRepo(),
				filePath);
	}
	
	private String getVersionFilesUrlFor(AddOnToIndex addOnToIndex, String versionString) {
		BintrayPackageDetails details = addOnToIndex.getBintrayPackageDetails();
		return String.format("https://bintray.com/api/v1/packages/%s/%s/%s/versions/%s/files?include_unpublished=0",
				details.getOwner(),
				details.getRepo(),
				details.getPackageName(),
				versionString);
	}
	
	private String hostedUrlFor(AddOnToIndex addOnToIndex) {
		BintrayPackageDetails details = addOnToIndex.getBintrayPackageDetails();
		return String.format("https://bintray.com/%s/%s/%s",
				details.getOwner(),
				details.getRepo(),
				details.getPackageName());
	}
	
	private String packageUrlFor(AddOnToIndex addOnToIndex) {
		BintrayPackageDetails details = addOnToIndex.getBintrayPackageDetails();
		return String.format("https://bintray.com/api/v1/packages/%s/%s/%s",
				details.getOwner(),
				details.getRepo(),
				details.getPackageName());
	}
}
