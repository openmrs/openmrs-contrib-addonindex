package org.openmrs.addonindex.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.VersionList;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.service.VersionsService;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CoreVersionsControllerIT {

	@LocalServerPort
	private int port;

	@MockBean
    @SuppressWarnings("unused")
	private Index index;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@MockBean
	private VersionsService versionsService;

	@BeforeEach
	public void setUp() {
		List<String> versions = new ArrayList<>();
		versions.add("1.6.3");
		versions.add("1.6.4");
		VersionList versionList;
		versionList = new VersionList(versions);
		doReturn(versionList).when(versionsService).getVersions();
	}

	@Test
	public void getCoreversions() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/coreversions",
				String.class);

		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("[\"1.6.3\",\"1.6.4\"]", entity.getBody(), false);
	}
}
