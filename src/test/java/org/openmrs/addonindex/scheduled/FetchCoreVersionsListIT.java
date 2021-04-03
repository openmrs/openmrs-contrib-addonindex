package org.openmrs.addonindex.scheduled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.VersionList;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.service.VersionsService;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class FetchCoreVersionsListIT {

	@MockBean
	@SuppressWarnings("unused")
	private Index index;

	@MockBean
	private RestTemplateBuilder restTemplateBuilder;

	@MockBean
	private RestTemplate restTemplate;

	@Autowired
	private VersionsService versionsService;

	@Autowired
	private FetchCoreVersionsList fetchCoreVersionsList;

	@BeforeEach
	public void setUp() {
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
	}

	@Test
	public void testCoreVersionsJsonHandle() throws Exception {
		when(restTemplate.getForObject("https://openmrs.jfrog.io/openmrs/api/storage/public/org/openmrs/api/openmrs-api/",
				String.class)).thenReturn(getFileAsString("core-versions.json"));

		fetchCoreVersionsList.fetchCoreVersionsList();

		VersionList versionList = versionsService.getVersions();
		assertThat(versionList, notNullValue());

		Version testVersion = new Version("1.9.12-SNAPSHOT");
		assertThat(versionList.getVersions(), not(contains(testVersion)));
		assertThat(versionList.getVersions().first().toString(), is("1.6.3"));
	}
}
