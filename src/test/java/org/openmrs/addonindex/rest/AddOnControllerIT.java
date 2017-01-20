package org.openmrs.addonindex.rest;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.service.Index;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
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
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setUid("reporting-module");
		info.setName("Reporting Module");
		
		when(index.getAllByType(null)).thenReturn(singletonList(info));
		when(index.getByUid("reporting-module")).thenReturn(info);
	}
	
	@Test
	public void testGetAll() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/addon",
				String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("[{uid:\"reporting-module\",name:\"Reporting Module\"}]", entity.getBody(), false);
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
		JSONAssert.assertEquals("{uid:\"reporting-module\",name:\"Reporting Module\"}", entity.getBody(), false);
	}
}