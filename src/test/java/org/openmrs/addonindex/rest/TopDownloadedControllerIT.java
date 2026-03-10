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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.AnalysisService;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.util.Version;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TopDownloadedControllerIT {
	
	@LocalServerPort
	private int port;
	
	@MockitoBean
	private AnalysisService analysisService;
	
	@MockitoBean
	@SuppressWarnings("unused")
	private ElasticSearchIndex elasticSearchIndex;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@BeforeEach
	public void setUp() {
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
		info.setDownloadCountInLast30Days(123);
		info.addVersion(version);
		
		List<AddOnInfoSummaryAndStats> top = new ArrayList<>();
		top.add(new AddOnInfoSummaryAndStats(info));
		
		when(analysisService.getTopDownloaded()).thenReturn(top);
	}
	
	@Test
	public void getTopDownloaded() throws Exception {
		ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/topdownloaded",
		    String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		JSONAssert.assertEquals("[{summary:{uid:\"reporting-module\"}," + "downloadCount:123}]", entity.getBody(), false);
	}
}
