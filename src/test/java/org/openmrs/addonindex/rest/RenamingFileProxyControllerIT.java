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

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sun.net.httpserver.HttpServer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RenamingFileProxyControllerIT {
	
	private static final byte[] WAR_BYTES = "pretend this is a war".getBytes(StandardCharsets.UTF_8);
	
	@LocalServerPort
	private int port;
	
	@MockitoBean
	private Index index;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	private HttpServer upstream;
	
	@BeforeEach
	public void setUp() throws Exception {
		upstream = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		upstream.createContext("/openmrs-webapp-2.8.10.war", exchange -> {
			exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/java-archive");
			exchange.sendResponseHeaders(200, WAR_BYTES.length);
			exchange.getResponseBody().write(WAR_BYTES);
			exchange.close();
		});
		upstream.start();
		
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("2.8.10"));
		version.setDownloadUri("http://localhost:" + upstream.getAddress().getPort() + "/openmrs-webapp-2.8.10.war");
		version.setRenameTo("openmrs.war");
		
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setUid("org.openmrs.core");
		info.addVersion(version);
		when(index.getByUid("org.openmrs.core")).thenReturn(info);
	}
	
	@AfterEach
	public void tearDown() {
		upstream.stop(0);
	}
	
	@Test
	public void shouldProxyTheFileAsAnAttachmentWithItsNewName() {
		ResponseEntity<byte[]> entity = testRestTemplate
		        .getForEntity("http://localhost:" + port + "/api/v1/addon/org.openmrs.core/2.8.10/download", byte[].class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.OK));
		assertThat(entity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION),
		    is("attachment; filename=\"openmrs.war\""));
		// server.servlet.encoding.force-response appends a charset, so compare type/subtype only
		assertThat(
		    entity.getHeaders().getContentType().equalsTypeAndSubtype(MediaType.parseMediaType("application/java-archive")),
		    is(true));
		assertThat(entity.getHeaders().getContentLength(), is((long) WAR_BYTES.length));
		assertThat(entity.getBody(), is(WAR_BYTES));
	}
	
	@Test
	public void shouldReturnNotFoundForAnUnknownVersion() {
		ResponseEntity<String> entity = testRestTemplate
		        .getForEntity("http://localhost:" + port + "/api/v1/addon/org.openmrs.core/9.9.9/download", String.class);
		
		assertThat(entity.getStatusCode(), is(HttpStatus.NOT_FOUND));
	}
}
