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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.artifactory.AqlSearchResponse;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryArtifactDetails;
import org.openmrs.addonindex.domain.artifactory.GavcSearchResponse;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

@JsonTest
class ArtifactoryTest {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private RestTemplate restTemplate;
	
	private Artifactory artifactory;
	
	@BeforeEach
	public void setUp() {
	}
	
	@Test
	public void shouldHandleOmodWithoutCredentials() throws Exception {
		// setup
		artifactory = new Artifactory(restTemplate, null);
		
		@SuppressWarnings("unchecked")
		ResponseEntity<GavcSearchResponse> searchResponse = mock(ResponseEntity.class);
		
		when(restTemplate.getForEntity(eq(Artifactory.GAVC_URL), eq(GavcSearchResponse.class), anyMap()))
		        .thenReturn(searchResponse);
		when(searchResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(searchResponse.getBody())
		        .thenReturn(objectMapper.readValue(getFileAsString("artifactory-gavc-uri.json"), GavcSearchResponse.class));
		
		when(restTemplate.getForObject(eq(
		    "https://openmrs.jfrog.io/artifactory/api/storage/modules/org/openmrs/module/fhir2-omod/1.0.0/fhir2-omod-1.0.0.jar"),
		    eq(ArtifactoryArtifactDetails.class))).thenReturn(
		        objectMapper.readValue(getFileAsString("artifactory-gavc-module.json"), ArtifactoryArtifactDetails.class));
		
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.OMOD);
		MavenRepoDetails repoDetails = new MavenRepoDetails("org.openmrs.module", "fhir2");
		addOnToIndex.setMavenRepoDetails(repoDetails);
		
		// execution
		AddOnInfoAndVersions infoAndVersions = artifactory.getInfoAndVersionsFor(addOnToIndex);
		
		// assertions
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.0.0"));
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(), equalTo(
		    "https://openmrs.jfrog.io/artifactory/modules/org/openmrs/module/fhir2-omod/1.0.0/fhir2-omod-1.0.0.jar"));
		assertThat(infoAndVersions.getVersions().get(0).getRenameTo(), equalTo("fhir2-1.0.0.omod"));
		assertThat(infoAndVersions.getVersions().get(0).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2020-10-01T21:13:09.787Z")));
	}
	
	@Test
	public void shouldHandleOmodWithCredentials() throws Exception {
		// setup
		artifactory = new Artifactory(restTemplate, "some_api_key");
		
		@SuppressWarnings("unchecked")
		ResponseEntity<AqlSearchResponse> searchResponse = mock(ResponseEntity.class);
		
		when(restTemplate.execute(eq(Artifactory.AQL_URL), eq(HttpMethod.POST), any(), any())).thenReturn(searchResponse);
		when(searchResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(searchResponse.getBody()).thenReturn(
		    objectMapper.readValue(getFileAsString("artifactory-aql-response.json"), AqlSearchResponse.class));
		
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.OMOD);
		MavenRepoDetails repoDetails = new MavenRepoDetails("org.openmrs.module", "fhir2");
		addOnToIndex.setMavenRepoDetails(repoDetails);
		
		// execution
		AddOnInfoAndVersions infoAndVersions = artifactory.getInfoAndVersionsFor(addOnToIndex);
		
		// assertions
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.0.0"));
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(), equalTo(
		    "https://openmrs.jfrog.io/artifactory/modules/org/openmrs/module/fhir2-omod/1.0.0/fhir2-omod-1.0.0.jar"));
		assertThat(infoAndVersions.getVersions().get(0).getRenameTo(), equalTo("fhir2-1.0.0.omod"));
		assertThat(infoAndVersions.getVersions().get(0).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2020-10-01T21:13:09.787Z")));
	}
	
	@Test
	public void shouldHandleOwaWithCredentials() throws Exception {
		artifactory = new Artifactory(restTemplate, "some_api_key");
		
		@SuppressWarnings("unchecked")
		ResponseEntity<AqlSearchResponse> searchResponse = mock(ResponseEntity.class);
		
		when(restTemplate.execute(eq(Artifactory.AQL_URL), eq(HttpMethod.POST), any(), any())).thenReturn(searchResponse);
		when(searchResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(searchResponse.getBody()).thenReturn(
		    objectMapper.readValue(getFileAsString("artifactory-aql-owa-response.json"), AqlSearchResponse.class));
		
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.OWA);
		MavenRepoDetails repoDetails = new MavenRepoDetails("org.openmrs.owa", "conceptdictionary");
		addOnToIndex.setMavenRepoDetails(repoDetails);
		
		// execution
		AddOnInfoAndVersions infoAndVersions = artifactory.getInfoAndVersionsFor(addOnToIndex);
		
		// assertions
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.0.0"));
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(), equalTo(
		    "https://openmrs.jfrog.io/artifactory/owa/org/openmrs/owa/conceptdictionary/1.0.0/conceptdictionary-1.0.0.zip"));
		assertThat(infoAndVersions.getVersions().get(0).getRenameTo(), equalTo("conceptdictionary-1.0.0.zip"));
	}
	
	@Test
	public void shouldHandleContentPackageWithCredentials() throws Exception {
		// setup
		artifactory = new Artifactory(restTemplate, "some_api_key");
		
		@SuppressWarnings("unchecked")
		ResponseEntity<AqlSearchResponse> searchResponse = mock(ResponseEntity.class);
		
		when(restTemplate.execute(eq(Artifactory.AQL_URL), eq(HttpMethod.POST), any(), any())).thenReturn(searchResponse);
		when(searchResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(searchResponse.getBody()).thenReturn(
		    objectMapper.readValue(getFileAsString("artifactory-aql-zip-response.json"), AqlSearchResponse.class));
		
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.CONTENT_PACKAGE);
		MavenRepoDetails repoDetails = new MavenRepoDetails("org.openmrs.content", "referenceapplication");
		addOnToIndex.setMavenRepoDetails(repoDetails);
		
		// execution
		AddOnInfoAndVersions infoAndVersions = artifactory.getInfoAndVersionsFor(addOnToIndex);
		
		// assertions
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.4.0"));
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(), equalTo(
		    "https://openmrs.jfrog.io/artifactory/releases/org/openmrs/content/referenceapplication/1.4.0/referenceapplication-1.4.0.zip"));
		assertThat(infoAndVersions.getVersions().get(0).getRenameTo(), equalTo("referenceapplication-1.4.0.zip"));
		assertThat(infoAndVersions.getVersions().get(0).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2025-09-01T10:00:00.000Z")));
	}
	
	@Test
	public void shouldHandleContentPackageWithoutCredentials() throws Exception {
		// setup
		artifactory = new Artifactory(restTemplate, null);
		
		@SuppressWarnings("unchecked")
		ResponseEntity<GavcSearchResponse> searchResponse = mock(ResponseEntity.class);
		
		// matching on repos=releases also asserts the GAVC search targets the releases repo
		when(restTemplate.getForEntity(eq(Artifactory.GAVC_URL), eq(GavcSearchResponse.class),
		    eq(Map.of("g", "org.openmrs.content", "a", "referenceapplication", "repos", "releases"))))
		        .thenReturn(searchResponse);
		when(searchResponse.getStatusCode()).thenReturn(HttpStatus.OK);
		when(searchResponse.getBody()).thenReturn(
		    objectMapper.readValue(getFileAsString("artifactory-gavc-zip-uri.json"), GavcSearchResponse.class));
		
		when(restTemplate.getForObject(eq(
		    "https://openmrs.jfrog.io/artifactory/api/storage/releases/org/openmrs/content/referenceapplication/1.4.0/referenceapplication-1.4.0.zip"),
		    eq(ArtifactoryArtifactDetails.class)))
		        .thenReturn(objectMapper.readValue(getFileAsString("artifactory-gavc-content-module.json"),
		            ArtifactoryArtifactDetails.class));
		
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.CONTENT_PACKAGE);
		MavenRepoDetails repoDetails = new MavenRepoDetails("org.openmrs.content", "referenceapplication");
		addOnToIndex.setMavenRepoDetails(repoDetails);
		
		// execution
		AddOnInfoAndVersions infoAndVersions = artifactory.getInfoAndVersionsFor(addOnToIndex);
		
		// assertions
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.4.0"));
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(), equalTo(
		    "https://openmrs.jfrog.io/artifactory/releases/org/openmrs/content/referenceapplication/1.4.0/referenceapplication-1.4.0.zip"));
		assertThat(infoAndVersions.getVersions().get(0).getRenameTo(), equalTo("referenceapplication-1.4.0.zip"));
		assertThat(infoAndVersions.getVersions().get(0).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2025-09-01T10:00:00.000Z")));
	}
	
}
