/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.TestUtil;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * The underlying functionality here is too trivial to even need a test, but I wrote this to see if
 * Spring would correctly handle my returning Java 8 Optional from a RestController (and return a
 * 404 it's not present). That didn't seem to work.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ListControllerIT {
	
	@LocalServerPort
	private int port;
	
	@MockitoBean
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
		ResponseEntity<String> entity = testRestTemplate
		        .getForEntity("http://localhost:" + port + "/api/v1/list/highlighted", String.class);
		
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
