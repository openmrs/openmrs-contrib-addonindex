package org.openmrs.addonindex.backend;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

import java.time.OffsetDateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.util.Version;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class BintrayTest {

	@Mock
	private RestTemplateBuilder builder;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private Bintray bintray;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(builder.basicAuthorization(anyString(), anyString())).thenReturn(builder);
		when(builder.build()).thenReturn(restTemplate);
	}

	@Test
	public void testHandleJson() throws Exception {
		String json = getFileAsString("bintray-package.json");

		when(restTemplate.getForObject("https://bintray.com/api/v1/packages/openmrs/owa/openmrs-owa-conceptdictionary/"
				+ "versions/1.0.0/files?include_unpublished=0", ArrayNode.class))
				.thenReturn(new ObjectMapper().readValue(getFileAsString("bintray-version-files.json"), ArrayNode.class));

		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.OWA);
		addOnToIndex.setUid("conceptdictionary-owa");
		addOnToIndex.setBackend(Bintray.class);
		addOnToIndex.setBintrayPackageDetails(new BintrayPackageDetails("openmrs", "owa",
				"openmrs-owa-conceptdictionary"));

		AddOnInfoAndVersions info = bintray.handlePackageJson(addOnToIndex, json);
		assertThat(info.getName(), is("openmrs-owa-conceptdictionary"));
		assertThat(info.getDescription(),
				is("Provides web interface for managing concept dictionary in OpenMRS distributions. Requires Open Web App "
						+ "and Webservices.rest modules."));
		assertThat(info.getHostedUrl(), is("https://bintray.com/openmrs/owa/openmrs-owa-conceptdictionary"));
		assertThat(info.getVersionCount(), is(1));
		assertThat(info.getVersions().get(0), is(allOf(
				hasProperty("version", equalTo(new Version("1.0.0"))),
				hasProperty("releaseDatetime", is(OffsetDateTime.parse("2016-09-12T18:51:14.574Z"))),
				hasProperty("downloadUri",
						is("https://dl.bintray.com/openmrs/owa/conceptdictionary-1.0.0.zip"))
		)));
	}

}