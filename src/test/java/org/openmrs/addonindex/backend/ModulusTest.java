package org.openmrs.addonindex.backend;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
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

public class ModulusTest {
	
	@Mock
	private RestTemplateBuilder restTemplateBuilder;
	
	@Mock
	private RestTemplate restTemplate;
	
	@InjectMocks
	private Modulus modulus;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
	}
	
	@Test
	public void testHandleModuleJson() throws Exception {
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.OWA);
		addOnToIndex.setUid("org.openmrs.module.addresshierarchy");
		addOnToIndex.setBackend(Modulus.class);
		addOnToIndex.setModulusDetails(new ModulusModuleDetails("3"));
		
		String json = getFileAsString("modulus-module.json");
		
		when(restTemplate.getForObject("https://modules.openmrs.org/modulus/api/modules/3/releases?max=1000",
				ArrayNode.class))
				.thenReturn(new ObjectMapper().readValue(getFileAsString("modulus-releases.json"), ArrayNode.class));
		
		AddOnInfoAndVersions info = modulus.handleModuleJson(addOnToIndex, json);
		assertThat(info.getName(), is("Address Hierarchy"));
		assertThat(info.getDescription(),
				is("Allows for the entry of structured addresses."));
		assertThat(info.getHostedUrl(), is("https://modules.openmrs.org/#/show/3"));
		assertThat(info.getVersionCount(), is(2));
		assertThat(info.getVersions().get(0), is(allOf(
				hasProperty("version", equalTo(new Version("1.1"))),
				hasProperty("releaseDatetime", is(OffsetDateTime.parse("2009-10-13T10:58:12Z"))),
				hasProperty("downloadUri",
						is("https://modules.openmrs.org/modulus/api/releases/6/download/addresshierarchy-1.1.omod"))
		)));
		assertThat(info.getVersions().get(1), is(allOf(
				hasProperty("version", equalTo(new Version("1.0"))),
				hasProperty("releaseDatetime", is(OffsetDateTime.parse("2008-09-11T08:44:32Z"))),
				hasProperty("downloadUri",
						is("https://modules.openmrs.org/modulus/api/releases/5/download/addresshierarchy-1.0.omod"))
		)));
	}
	
}