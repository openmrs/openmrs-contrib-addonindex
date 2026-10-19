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

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryArtifactDetails;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfo;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

@JsonTest
class ArtifactoryVersionFoldersTest {
	
	private static final String FOLDER_URL = "https://openmrs.jfrog.io/artifactory/api/storage/releases/org/openmrs/web/openmrs-webapp/";
	
	private static final String WAR_STORAGE_URL = "https://openmrs.jfrog.io/artifactory/api/storage/releases/org/openmrs/web/openmrs-webapp/2.8.10/openmrs-webapp-2.8.10.war";
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private RestTemplate restTemplate;
	
	private ArtifactoryVersionFolders backend;
	
	@BeforeEach
	public void setUp() {
		backend = new ArtifactoryVersionFolders(restTemplate);
	}
	
	private AddOnToIndex coreToIndex() {
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setUid("org.openmrs.core");
		addOnToIndex.setType(AddOnType.CORE);
		addOnToIndex.setMavenRepoDetails(new MavenRepoDetails("org.openmrs.web", "openmrs-webapp"));
		return addOnToIndex;
	}
	
	@Test
	public void shouldListReleasedVersionFoldersAsDownloadableWars() throws Exception {
		when(restTemplate.getForObject(eq(FOLDER_URL), eq(ArtifactoryFolderInfo.class))).thenReturn(
		    objectMapper.readValue(getFileAsString("artifactory-folder-webapp.json"), ArtifactoryFolderInfo.class));
		
		AddOnInfoAndVersions infoAndVersions = backend.getInfoAndVersionsFor(coreToIndex());
		
		// pre-release folders and non-folder children are skipped; versions are sorted
		assertThat(infoAndVersions.getVersions(), contains(hasProperty("version", equalTo(new Version("2.8.9"))),
		    hasProperty("version", equalTo(new Version("2.8.10")))));
		AddOnVersion latest = infoAndVersions.getVersions().get(1);
		assertThat(latest.getDownloadUri(), equalTo(
		    "https://openmrs.jfrog.io/artifactory/releases/org/openmrs/web/openmrs-webapp/2.8.10/openmrs-webapp-2.8.10.war"));
		assertThat(latest.getRenameTo(), equalTo("openmrs.war"));
		// left for fetchVersionDetails, so already-indexed versions cost no request
		assertThat(infoAndVersions.getVersions(), everyItem(hasProperty("releaseDatetime", nullValue())));
	}
	
	@Test
	public void shouldNameDownloadsByVersionForTypesOtherThanCore() throws Exception {
		when(restTemplate.getForObject(eq(FOLDER_URL), eq(ArtifactoryFolderInfo.class))).thenReturn(
		    objectMapper.readValue(getFileAsString("artifactory-folder-webapp.json"), ArtifactoryFolderInfo.class));
		AddOnToIndex addOnToIndex = coreToIndex();
		addOnToIndex.setType(AddOnType.CONTENT_PACKAGE);
		
		AddOnInfoAndVersions infoAndVersions = backend.getInfoAndVersionsFor(addOnToIndex);
		
		assertThat(infoAndVersions.getVersions().get(1).getRenameTo(), equalTo("openmrs-webapp-2.8.10.zip"));
	}
	
	@Test
	public void shouldFailWhenNoFolderListingIsReturned() {
		assertThrows(IllegalStateException.class, () -> backend.getInfoAndVersionsFor(coreToIndex()));
	}
	
	@Test
	public void shouldSetReleaseDateFromStorageApi() throws Exception {
		when(restTemplate.getForObject(eq(WAR_STORAGE_URL), eq(ArtifactoryArtifactDetails.class))).thenReturn(objectMapper
		        .readValue(getFileAsString("artifactory-storage-webapp-war.json"), ArtifactoryArtifactDetails.class));
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("2.8.10"));
		
		backend.fetchVersionDetails(coreToIndex(), version);
		
		assertThat(version.getReleaseDatetime(), equalTo(OffsetDateTime.parse("2026-10-05T16:56:25.370Z")));
	}
	
	@Test
	public void shouldThrowWhenTheVersionFolderHasNoMatchingFile() {
		when(restTemplate.getForObject(eq(WAR_STORAGE_URL), eq(ArtifactoryArtifactDetails.class)))
		        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("2.8.10"));
		
		assertThrows(HttpClientErrorException.class, () -> backend.fetchVersionDetails(coreToIndex(), version));
	}
}
