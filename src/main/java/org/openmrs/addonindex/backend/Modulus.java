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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Indexes files from the (soon-to-be-legacy) modules.openmrs.org
 */
@Component
@Slf4j
public class Modulus implements BackendHandler {

	private final RestTemplateBuilder restTemplateBuilder;

	private final ObjectMapper objectMapper;
	
	@Autowired
	public Modulus(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
		this.restTemplateBuilder = restTemplateBuilder;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String moduleUrl = moduleUrlFor(addOnToIndex);
		ResponseEntity<String> entity = restTemplateBuilder.build().getForEntity(moduleUrl, String.class);
		if (!entity.getStatusCode().is2xxSuccessful()) {
			log.warn("Problem fetching {} -> {} {}", moduleUrl, entity.getStatusCode(), entity.getBody());
			return null;
		} else {
			String json = entity.getBody();
			return handleModuleJson(addOnToIndex, json);
		}
	}
	
	AddOnInfoAndVersions handleModuleJson(AddOnToIndex addOnToIndex, String moduleJson) throws IOException {
		AddOnInfoAndVersions info = AddOnInfoAndVersions.from(addOnToIndex);
		info.setHostedUrl(hostedUrlFor(addOnToIndex));

		ObjectNode obj = objectMapper.readValue(moduleJson, ObjectNode.class);
		
		if (!StringUtils.hasText(info.getName())) {
			info.setName(obj.get("name").asText());
		}
		if (!StringUtils.hasText(info.getDescription())) {
			info.setDescription(obj.get("description").asText());
		}
		
		ArrayNode releases = restTemplateBuilder.build().getForObject(releasesUrlFor(addOnToIndex), ArrayNode.class);
		if (releases != null) {
			for (JsonNode releaseNode : releases) {
				if (releaseNode.path("moduleVersion").isNull()) {
					log.debug("Invalid release in modulus for {}", addOnToIndex.getUid());
					continue;
				}

				AddOnVersion version = new AddOnVersion();
				version.setVersion(new Version(releaseNode.path("moduleVersion").asText()));
				version.setReleaseDatetime(OffsetDateTime.parse(releaseNode.path("dateCreated").asText()));
				version.setDownloadUri(releaseNode.path("downloadURL").asText());
				info.addVersion(version);
			}
		}
		
		return info;
	}
	
	private String hostedUrlFor(AddOnToIndex addOnToIndex) {
		return String.format("https://modules.openmrs.org/#/show/%s", addOnToIndex.getModulusDetails().getId());
	}
	
	private String moduleUrlFor(AddOnToIndex addOnToIndex) {
		return String.format("https://modules.openmrs.org/modulus/api/modules/%s", addOnToIndex.getModulusDetails().getId
				());
	}
	
	private String releasesUrlFor(AddOnToIndex addOnToIndex) {
		return String.format("https://modules.openmrs.org/modulus/api/modules/%s/releases?max=1000",
				addOnToIndex.getModulusDetails().getId());
	}
}
