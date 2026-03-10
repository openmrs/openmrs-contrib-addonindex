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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CoreVersionsControllerIT {
	
	@LocalServerPort
	private int port;
	
	@MockitoBean
	@SuppressWarnings("unused")
	private Index index;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@MockitoBean
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
