/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.configuration.jackson;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

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
