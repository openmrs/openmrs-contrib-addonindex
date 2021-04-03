package org.openmrs.addonindex.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@JsonTest
class ArtifactoryTest {

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
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
		when(searchResponse.getBody()).thenReturn(objectMapper.readValue(
				getFileAsString("artifactory-gavc-uri.json"), GavcSearchResponse.class
		));

		when(restTemplate.getForObject(
				eq("https://openmrs.jfrog.io/artifactory/api/storage/modules/org/openmrs/module/fhir2-omod/1.0.0/fhir2-omod-1.0.0.jar"),
				eq(ArtifactoryArtifactDetails.class)))
				.thenReturn(objectMapper.readValue(
						getFileAsString("artifactory-gavc-module.json"), ArtifactoryArtifactDetails.class
				));

		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.OMOD);
		MavenRepoDetails repoDetails = new MavenRepoDetails("org.openmrs.module", "fhir2");
		addOnToIndex.setMavenRepoDetails(repoDetails);

		// execution
		AddOnInfoAndVersions infoAndVersions = artifactory.getInfoAndVersionsFor(addOnToIndex);

		// assertions
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.0.0"));
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(),
				equalTo("https://openmrs.jfrog.io/artifactory/modules/org/openmrs/module/fhir2-omod/1.0.0/fhir2-omod-1.0.0.jar"));
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

		when(restTemplate.execute(eq(Artifactory.AQL_URL), eq(HttpMethod.POST), any(), any()))
				.thenReturn(searchResponse);
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
		assertThat(infoAndVersions.getVersions().get(0).getDownloadUri(),
				equalTo("https://openmrs.jfrog.io/artifactory/modules/org/openmrs/module/fhir2-omod/1.0.0/fhir2-omod-1.0.0.jar"));
		assertThat(infoAndVersions.getVersions().get(0).getRenameTo(), equalTo("fhir2-1.0.0.omod"));
		assertThat(infoAndVersions.getVersions().get(0).getReleaseDatetime(),
				equalTo(OffsetDateTime.parse("2020-10-01T21:13:09.787Z")));
	}

}
