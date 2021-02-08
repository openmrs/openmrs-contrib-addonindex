package org.openmrs.addonindex.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.TestUtil;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The underlying functionality here is too trivial to even need a test, but I wrote this to see if Spring would correctly
 * handle my returning Java 8 Optional from a RestController (and return a 404 it's not present). That didn't seem to work.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ListControllerIT {
	
	@LocalServerPort
	private int port;
	
	@MockBean
	@SuppressWarnings("unused")
	private ElasticSearchIndex elasticSearchIndex;
	
	@Autowired
	private IndexingService indexingService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@BeforeEach
	public void setUp() throws Exception {
		TestUtil.loadLocalAddOnsToIndex(objectMapper, indexingService);
	}
	
	@Test
	public void testGetOne() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity(
				"http://localhost:" + port + "/api/v1/list/highlighted",
				String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("{uid:\"highlighted\"}", entity.getBody(), false);
	}
	
	@Test
	public void testGetOneNotFound() {
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/list/not_found",
				String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}
}
