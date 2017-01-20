package org.openmrs.addonindex.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AddOnControllerIT {
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Test
	public void testGetAll() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/addon",
				String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		assertThat(entity.getBody(), is("[]"));
	}
}