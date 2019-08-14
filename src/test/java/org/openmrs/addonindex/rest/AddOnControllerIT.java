package org.openmrs.addonindex.rest;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.OffsetDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.util.Version;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AddOnControllerIT {
	
	@LocalServerPort
	private int port;
	
	@MockBean
	private Index index;
	
	@Autowired
	private AddOnController controller;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Before
	public void setUp() throws Exception {
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("1.0"));
		version.setReleaseDatetime(OffsetDateTime.parse("2016-09-12T18:51:14.574Z"));
		version.setDownloadUri("http://www.google.com");
		
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setUid("reporting-module");
		info.setModuleId("1");
		info.setModulePackage("org.openmrs.module.reporting-module");
		info.setName("Reporting Module");
		info.setDescription("For reporting");
		info.addVersion(version);
		
		when(index.search(null, "report", null)).thenReturn(singletonList(new AddOnInfoSummary(info)));
		when(index.getByModulePackage("org.openmrs.module.reporting-module")).thenReturn(info);
		when(index.getByUid("reporting-module")).thenReturn(info);
		when(index.getRecentReleases(1)).thenReturn(singletonList(info));
	}
	
	@Test
	public void testSearch() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/addon?q=report",
				String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("[{uid:\"reporting-module\","
						+ "name:\"Reporting Module\","
						+ "description:\"For reporting\","
						+ "versionCount:1,"
						+ "latestVersion:\"1.0\"}]",
				entity.getBody(), false);
	}
	
	@Test
	public void testCors() throws Exception {
		String origin = "http://openmrs.org/news";
		ResponseEntity<String> entity = testRestTemplate.exchange(RequestEntity.get(
				uri("http://localhost:" + port + "/api/v1/addon?q=report"))
				.header(HttpHeaders.ORIGIN, origin).build(), String.class);
		
		assertThat(entity.getHeaders().getAccessControlAllowOrigin(), is(origin));
	}
	
	@Test
	public void getByModulePackage() throws Exception{
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/addon?&modulePackage=org.openmrs.module.reporting-module",
				String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("{uid:\"reporting-module\","
						+ "\"modulePackage\":org.openmrs.module.reporting-module,"
						+ "\"moduleId\":\"1\","
						+ "name:\"Reporting Module\","
						+ "description:\"For reporting\","
						+ "versionCount:1,"
						+ "latestVersion:\"1.0\","
						+ "versions:[{"
						+ "version:\"1.0\","
						+ "releaseDatetime:\"2016-09-12T18:51:14.574Z\","
						+ "downloadUri:\"http://www.google.com\""
						+ "}]}",
				entity.getBody(), false);
	}

	@Test
	public void getByModulePackageNotFound() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity(
				"http://localhost:" + port + "/api/v1/addon?&modulePackage=fake-module",
				String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void getOneNotFound() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity(
				"http://localhost:" + port + "/api/v1/addon/fake-module",
				String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}

	@Test
	public void getOne() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity(
				"http://localhost:" + port + "/api/v1/addon/reporting-module",
				String.class);
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("{uid:\"reporting-module\","
						+ "name:\"Reporting Module\","
						+ "description:\"For reporting\","
						+ "versionCount:1,"
						+ "latestVersion:\"1.0\","
						+ "versions:[{"
						+ "version:\"1.0\","
						+ "releaseDatetime:\"2016-09-12T18:51:14.574Z\","
						+ "downloadUri:\"http://www.google.com\""
						+ "}]}",
				entity.getBody(), false);
	}
	
	private URI uri(String path) {
		return testRestTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
	}
}
