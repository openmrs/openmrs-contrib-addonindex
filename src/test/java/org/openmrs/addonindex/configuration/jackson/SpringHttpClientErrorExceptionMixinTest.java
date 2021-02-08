package org.openmrs.addonindex.configuration.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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
