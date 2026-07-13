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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Fetches versions and metadata for O3 frontend modules published to the public npm registry. The
 * registry document lists every version with its tarball URL and publish time. We index stable
 * releases only (versions with a pre-release segment are skipped). Per-version backend requirements
 * are read later by {@code FetchDetailsToIndex} from routes.json inside the tarball.
 */
@Component
@Slf4j
public class NpmRegistry implements BackendHandler, SupportsDownloadCounts {
	
	public static final String REGISTRY_URL = "https://registry.npmjs.org/";
	
	public static final String DOWNLOADS_URL = "https://api.npmjs.org/downloads/point/last-month/";
	
	private final RestTemplateBuilder restTemplateBuilder;
	
	private final ObjectMapper objectMapper;
	
	@Autowired
	public NpmRegistry(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
		this.restTemplateBuilder = restTemplateBuilder;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String packageName = addOnToIndex.getNpmPackageDetails().getPackageName();
		String url = REGISTRY_URL + packageName;
		log.info("Getting info from {}", url);
		String json = restTemplateBuilder.build().getForObject(url, String.class);
		return handleRegistryJson(addOnToIndex, json);
	}
	
	AddOnInfoAndVersions handleRegistryJson(AddOnToIndex addOnToIndex, String json) throws IOException {
		AddOnInfoAndVersions info = AddOnInfoAndVersions.from(addOnToIndex);
		String packageName = addOnToIndex.getNpmPackageDetails().getPackageName();
		info.setHostedUrl("https://www.npmjs.com/package/" + packageName);
		
		ObjectNode root = objectMapper.readValue(json, ObjectNode.class);
		JsonNode times = root.path("time");
		JsonNode versionsNode = root.path("versions");
		versionsNode.fieldNames().forEachRemaining(versionString -> {
			if (versionString.contains("-")) {
				return; // skip pre-releases
			}
			AddOnVersion version = new AddOnVersion();
			version.setVersion(new Version(versionString));
			version.setDownloadUri(versionsNode.path(versionString).path("dist").path("tarball").asText());
			String time = times.path(versionString).asText(null);
			if (time != null) {
				version.setReleaseDatetime(OffsetDateTime.parse(time));
			}
			info.addVersion(version);
		});
		return info;
	}
	
	@Override
	public void fetchDownloadCounts(AddOnToIndex toIndex, AddOnInfoAndVersions infoAndVersions) {
		String url = DOWNLOADS_URL + toIndex.getNpmPackageDetails().getPackageName();
		try {
			String json = restTemplateBuilder.build().getForObject(url, String.class);
			infoAndVersions.setDownloadCountInLast30Days(parseDownloadCount(json));
		}
		catch (Exception ex) {
			log.error("Error fetching download counts for {}", toIndex.getUid(), ex);
		}
	}
	
	int parseDownloadCount(String json) throws IOException {
		return objectMapper.readValue(json, ObjectNode.class).path("downloads").asInt();
	}
}
