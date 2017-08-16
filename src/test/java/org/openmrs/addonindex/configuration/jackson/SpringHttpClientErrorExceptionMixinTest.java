package org.openmrs.addonindex.configuration.jackson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@JsonTest
public class SpringHttpClientErrorExceptionMixinTest {
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Test
	public void testHandlesSpringHttpClientErrorException() throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");
		objectMapper.writeValueAsString(e);
		// this test is successful as long as there is no exception
	}
}