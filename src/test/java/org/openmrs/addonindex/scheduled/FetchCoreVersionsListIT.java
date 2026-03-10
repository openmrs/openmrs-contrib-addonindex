/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.scheduled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.VersionList;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.service.VersionsService;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

@SpringBootTest
public class FetchCoreVersionsListIT {
	
	@MockitoBean
	@SuppressWarnings("unused")
	private Index index;
	
	@MockitoBean
	private RestTemplateBuilder restTemplateBuilder;
	
	@MockitoBean
	private RestTemplate restTemplate;
	
	@Autowired
	private VersionsService versionsService;
	
	@Autowired
	private FetchCoreVersionsList fetchCoreVersionsList;
	
	@BeforeEach
	public void setUp() {
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
	}
	
	@Test
	public void testCoreVersionsJsonHandle() throws Exception {
		when(restTemplate.getForObject("https://openmrs.jfrog.io/openmrs/api/storage/public/org/openmrs/api/openmrs-api/",
		    String.class)).thenReturn(getFileAsString("core-versions.json"));
		
		fetchCoreVersionsList.fetchCoreVersionsList();
		
		VersionList versionList = versionsService.getVersions();
		assertThat(versionList, notNullValue());
		
		Version testVersion = new Version("1.9.12-SNAPSHOT");
		assertThat(versionList.getVersions(), not(contains(testVersion)));
		assertThat(versionList.getVersions().first().toString(), is("1.6.3"));
	}
}
