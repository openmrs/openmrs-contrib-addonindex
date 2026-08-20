/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.backend.NpmPackageDetails;
import org.openmrs.addonindex.domain.npm.NpmDownloadCount;
import org.openmrs.addonindex.domain.npm.NpmPackument;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

@JsonTest
class NpmJsTest {
	
	private static final String TARBALL_131 = "https://registry.npmjs.org/@openmrs/esm-billing-app/-/esm-billing-app-1.3.1.tgz";
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private RestTemplate restTemplate;
	
	private NpmJs npm;
	
	@BeforeEach
	public void setUp() {
		npm = new NpmJs(restTemplate, objectMapper);
	}
	
	private AddOnToIndex billingApp() {
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.FRONTEND_MODULE);
		addOnToIndex.setName("@openmrs/esm-billing-app");
		addOnToIndex.setNpmPackageDetails(new NpmPackageDetails("@openmrs/esm-billing-app"));
		return addOnToIndex;
	}
	
	private NpmPackument packument() throws Exception {
		return objectMapper.readValue(getFileAsString("npm-packument.json"), NpmPackument.class);
	}
	
	private void stubRegistry(ResponseEntity<NpmPackument> response) {
		when(restTemplate.exchange(eq(NpmJs.REGISTRY_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(NpmPackument.class),
		    anyMap())).thenReturn(response);
	}
	
	private ResponseEntity<NpmPackument> okWithEtag(NpmPackument packument, String etag) {
		HttpHeaders headers = new HttpHeaders();
		headers.setETag(etag);
		return new ResponseEntity<>(packument, headers, HttpStatus.OK);
	}
	
	private void stubPackument() throws Exception {
		stubRegistry(new ResponseEntity<>(packument(), HttpStatus.OK));
	}
	
	@Test
	public void shouldKeepOnlyStableVersions() throws Exception {
		stubPackument();
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		// 1.3.2-pre.1928 is a CI pre-release and must be dropped
		assertThat(infoAndVersions.getVersions(), hasSize(2));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.3.0"));
		assertThat(infoAndVersions.getVersions().get(1).getVersion().toString(), equalTo("1.3.1"));
	}
	
	@Test
	public void shouldUseTarballAsDownloadUriAndNotSetRenameTo() throws Exception {
		stubPackument();
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getVersions().get(1).getDownloadUri(), equalTo(TARBALL_131));
		// the tarball is already sensibly named, so downloads bypass the renaming proxy
		assertThat(infoAndVersions.getVersions().get(1).getRenameTo(), nullValue());
	}
	
	@Test
	public void shouldReadReleaseDatetimeFromTimeMap() throws Exception {
		stubPackument();
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getVersions().get(1).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2026-04-14T11:02:47.512Z")));
	}
	
	@Test
	public void shouldUseThePackumentDescriptionWhenTheCatalogueHasNone() throws Exception {
		stubPackument();
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getDescription(), equalTo("Billing frontend app for O3"));
	}
	
	@Test
	public void shouldPreferTheCatalogueDescriptionOverThePackuments() throws Exception {
		stubPackument();
		AddOnToIndex addOnToIndex = billingApp();
		addOnToIndex.setDescription("Hand-written catalogue description");
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(addOnToIndex);
		
		assertThat(infoAndVersions.getDescription(), equalTo("Hand-written catalogue description"));
	}
	
	@Test
	public void shouldKeepTheDescriptionOnANotModifiedRun() throws Exception {
		stubRegistry(okWithEtag(packument(), "\"abc123\""));
		npm.getInfoAndVersionsFor(billingApp());
		// a 304 carries no body, so the description can only come from the cache
		stubRegistry(new ResponseEntity<>(null, HttpStatus.NOT_MODIFIED));
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getDescription(), equalTo("Billing frontend app for O3"));
	}
	
	@Test
	public void shouldNeverSetRequireOpenmrsVersion() throws Exception {
		stubPackument();
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getVersions().get(0).getRequireOpenmrsVersion(), nullValue());
		assertThat(infoAndVersions.getVersions().get(1).getRequireOpenmrsVersion(), nullValue());
	}
	
	@Test
	public void shouldThrowWhenNpmPackageDetailsAreMissing() {
		AddOnToIndex addOnToIndex = new AddOnToIndex();
		addOnToIndex.setType(AddOnType.FRONTEND_MODULE);
		addOnToIndex.setName("broken");
		
		assertThrows(IllegalStateException.class, () -> npm.getInfoAndVersionsFor(addOnToIndex));
	}
	
	@Test
	public void shouldThrowWhenTheRegistryReturnsNoPackument() {
		stubRegistry(new ResponseEntity<>(null, HttpStatus.OK));
		
		// an empty result indexed as a success would replace the whole document with nothing
		assertThrows(IllegalStateException.class, () -> npm.getInfoAndVersionsFor(billingApp()));
	}
	
	@Test
	public void shouldThrowWhenThePackumentHasNoStableVersions() throws Exception {
		NpmPackument packument = packument();
		// leave only the 1.3.2-pre.1928 pre-release behind
		packument.getVersions().remove("1.3.0");
		packument.getVersions().remove("1.3.1");
		stubRegistry(okWithEtag(packument, "\"abc123\""));
		
		assertThrows(IllegalStateException.class, () -> npm.getInfoAndVersionsFor(billingApp()));
		
		// and the empty list must not have been cached under the ETag
		stubRegistry(new ResponseEntity<>(null, HttpStatus.NOT_MODIFIED));
		assertThrows(IllegalStateException.class, () -> npm.getInfoAndVersionsFor(billingApp()));
	}
	
	@Test
	public void shouldSkipVersionsWithNoTarball() throws Exception {
		NpmPackument packument = packument();
		packument.getVersions().get("1.3.0").setDist(null);
		stubRegistry(new ResponseEntity<>(packument, HttpStatus.OK));
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), equalTo("1.3.1"));
	}
	
	@Test
	public void shouldRequestTheConfiguredPackage() throws Exception {
		stubPackument();
		
		npm.getInfoAndVersionsFor(billingApp());
		
		verify(restTemplate).exchange(eq(NpmJs.REGISTRY_URL), eq(HttpMethod.GET), any(HttpEntity.class),
		    eq(NpmPackument.class), eq(Map.of("package", "@openmrs/esm-billing-app")));
	}
	
	@Test
	public void shouldSetDownloadCountInLast30Days() throws Exception {
		when(restTemplate.getForObject(eq(NpmJs.DOWNLOADS_URL), eq(NpmDownloadCount.class), anyMap()))
		        .thenReturn(objectMapper.readValue(getFileAsString("npm-downloads.json"), NpmDownloadCount.class));
		
		AddOnInfoAndVersions infoAndVersions = new AddOnInfoAndVersions();
		
		npm.fetchDownloadCounts(billingApp(), infoAndVersions);
		
		assertThat(infoAndVersions.getDownloadCountInLast30Days(), equalTo(8858));
	}
	
	@Test
	public void shouldNotFailWhenDownloadCountsAreUnavailable() {
		when(restTemplate.getForObject(eq(NpmJs.DOWNLOADS_URL), eq(NpmDownloadCount.class), anyMap()))
		        .thenThrow(new RestClientException("503 Service Unavailable"));
		
		AddOnInfoAndVersions infoAndVersions = new AddOnInfoAndVersions();
		
		npm.fetchDownloadCounts(billingApp(), infoAndVersions);
		
		assertThat(infoAndVersions.getDownloadCountInLast30Days(), nullValue());
	}
	
	private HttpClientErrorException notFound() {
		return HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null);
	}
	
	private AddOnVersion versionToEnrich(String versionString) {
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version(versionString));
		version.setDownloadUri(
		    "https://registry.npmjs.org/@openmrs/esm-billing-app/-/esm-billing-app-" + versionString + ".tgz");
		return version;
	}
	
	private byte[] tarball(Map<String, String> entries) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (TarArchiveOutputStream tar = new TarArchiveOutputStream(new GzipCompressorOutputStream(bytes))) {
			for (Map.Entry<String, String> entry : entries.entrySet()) {
				byte[] content = entry.getValue().getBytes(StandardCharsets.UTF_8);
				TarArchiveEntry tarEntry = new TarArchiveEntry(entry.getKey());
				tarEntry.setSize(content.length);
				tar.putArchiveEntry(tarEntry);
				tar.write(content);
				tar.closeArchiveEntry();
			}
		}
		return bytes.toByteArray();
	}
	
	/**
	 * NpmJs streams the tarball through restTemplate.execute, so the stub hands the extractor a
	 * response whose body is the tarball bytes.
	 */
	private void stubTarball(byte[] tarball) throws IOException {
		ClientHttpResponse response = mock(ClientHttpResponse.class);
		when(response.getBody()).thenReturn(new ByteArrayInputStream(tarball));
		when(restTemplate.execute(eq(TARBALL_131), eq(HttpMethod.GET), isNull(), any()))
		        .thenAnswer(invocation -> invocation.<ResponseExtractor<String>> getArgument(3).extractData(response));
	}
	
	@Test
	public void shouldReadRoutesJsonOutOfTheVersionsTarball() throws Exception {
		stubTarball(tarball(Map.of("package/dist/routes.json", getFileAsString("routes.billing.json"))));
		
		AddOnVersion version = versionToEnrich("1.3.1");
		npm.fetchVersionDetails(billingApp(), version);
		
		assertThat(version.getRequireModules().size(), is(2));
	}
	
	@Test
	public void shouldIndexAVersionWhoseTarballShipsNoRoutesJson() throws Exception {
		stubTarball(tarball(Map.of("package/package.json", "{\"name\": \"@openmrs/esm-billing-app\"}")));
		
		AddOnVersion version = versionToEnrich("1.3.1");
		npm.fetchVersionDetails(billingApp(), version);
		
		// a frontend module without a routes.json declares no backend dependencies
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void shouldThrowWhenTheTarballCannotBeFetched() {
		// the registry serves a tarball for every version its packument lists, so a failure here is
		// transient: the caller drops the version for this run and a later run retries it
		when(restTemplate.execute(eq(TARBALL_131), eq(HttpMethod.GET), isNull(), any())).thenThrow(notFound());
		
		assertThrows(HttpClientErrorException.class, () -> npm.fetchVersionDetails(billingApp(), versionToEnrich("1.3.1")));
	}
	
	@Test
	public void testParsingRoutesJsonWithNullDependencyMapAsAbsent() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		// schema-invalid but harmless: an explicit null must not fail the version forever
		npm.handleRoutesJson("{ \"backendDependencies\": null }", version);
		
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void testParsingRoutesJsonForRequiredDependencies() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		npm.handleRoutesJson(getFileAsString("routes.billing.json"), version);
		// frontend modules never declare an OpenMRS core version
		assertThat(version.getRequireOpenmrsVersion(), nullValue());
		assertThat(version.getRequireModules().size(), is(2));
		// SemVer ranges are translated to the OpenMRS syntax the rest of the index uses
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.module.billing")), hasProperty("version", is("2.3.0")))));
		assertThat(version.getRequireModules(), hasItem(
		    allOf(hasProperty("module", is("org.openmrs.module.webservices.rest")), hasProperty("version", is("2.24.0")))));
	}
	
	@Test
	public void testParsingRoutesJsonIgnoresOptionalDependencies() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		// only backendDependencies are indexed, for parity with config.xml's require_module handling
		npm.handleRoutesJson(getFileAsString("routes.ward.json"), version);
		assertThat(version.getRequireModules().size(), is(2));
		assertThat(version.getRequireModules(), hasItem(
		    allOf(hasProperty("module", is("org.openmrs.module.webservices.rest")), hasProperty("version", is("2.2.0")))));
		assertThat(version.getRequireModules(), hasItem(
		    allOf(hasProperty("module", is("org.openmrs.module.emrapi")), hasProperty("version", is("2.0.0 - 2.*")))));
	}
	
	@Test
	public void testParsingRoutesJsonForObjectFormDependencies() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		// the object form contributes its "version" and its "feature" block is ignored
		npm.handleRoutesJson("{ \"backendDependencies\": { \"bedmanagement\": { \"version\": \">=6.0.0 <8.0.0\","
		        + " \"feature\": { \"flagName\": \"bedmanagement-module\" } } } }",
		    version);
		assertThat(version.getRequireModules().size(), is(1));
		assertThat(version.getRequireModules(), hasItem(allOf(hasProperty("module", is("org.openmrs.module.bedmanagement")),
		    hasProperty("version", is("6.0.0 - 7.*")))));
	}
	
	@Test
	public void testParsingRoutesJsonWithUntranslatableRangeRecordsUnknownVersion() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		// an || union has no OpenMRS equivalent; the module must still be recorded, with an
		// unknown version, rather than dropped or allowed to fail the whole version
		npm.handleRoutesJson("{ \"backendDependencies\": { \"billing\": \">=1.0.0 || ^2\" } }", version);
		
		assertThat(version.getRequireModules().size(), is(1));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.module.billing")), hasProperty("version", is("?")))));
	}
	
	@Test
	public void testParsingRoutesJsonWithOnlyOptionalDependencies() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		npm.handleRoutesJson(getFileAsString("routes.patientChart.json"), version);
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void testParsingRoutesJsonWithNoDependencyMapsAtAll() throws Exception {
		AddOnVersion version = new AddOnVersion();
		
		npm.handleRoutesJson(getFileAsString("routes.noDependencies.json"), version);
		assertThat(version.getRequireModules(), nullValue());
		assertThat(version.getRequireOpenmrsVersion(), nullValue());
	}
	
	@Test
	public void testParsingMalformedRoutesJsonThrows() {
		AddOnVersion version = new AddOnVersion();
		
		// the caller catches this per version and keeps going
		assertThrows(IOException.class, () -> npm.handleRoutesJson("{ not json", version));
	}
	
	@Test
	public void testParsingRoutesJsonWithNonObjectRootThrows() {
		AddOnVersion version = new AddOnVersion();
		
		// valid JSON that is not an object must not be indexed as "declares no dependencies"
		assertThrows(IOException.class, () -> npm.handleRoutesJson("[\"not\", \"an\", \"object\"]", version));
	}
	
	@Test
	public void testParsingRoutesJsonWithNonObjectDependencyMapThrows() {
		AddOnVersion version = new AddOnVersion();
		
		assertThrows(IOException.class,
		    () -> npm.handleRoutesJson("{ \"backendDependencies\": [\"webservices.rest\"] }", version));
	}
	
	@Test
	public void testParsingRoutesJsonWithVersionlessDependencyThrows() {
		AddOnVersion version = new AddOnVersion();
		
		// an object-form dependency must carry a textual "version"; indexing it as "?" hides bad data
		assertThrows(IOException.class,
		    () -> npm.handleRoutesJson("{ \"backendDependencies\": { \"billing\": {} } }", version));
	}
	
	@Test
	public void shouldToleratePerVersionUnparseablePublishTimes() throws Exception {
		NpmPackument packument = packument();
		packument.getTime().put("1.3.0", "not-a-timestamp");
		stubRegistry(new ResponseEntity<>(packument, HttpStatus.OK));
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		// one bad timestamp must cost only that version's release date, never the whole app
		assertThat(infoAndVersions.getVersions(), hasSize(2));
		assertThat(infoAndVersions.getVersions().get(0).getReleaseDatetime(), nullValue());
		assertThat(infoAndVersions.getVersions().get(1).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2026-04-14T11:02:47.512Z")));
	}
	
	@Test
	public void shouldSendNoIfNoneMatchUntilItHasSeenAnEtag() throws Exception {
		stubPackument();
		
		npm.getInfoAndVersionsFor(billingApp());
		
		ArgumentCaptor<HttpEntity<Void>> request = ArgumentCaptor.captor();
		verify(restTemplate).exchange(eq(NpmJs.REGISTRY_URL), eq(HttpMethod.GET), request.capture(), eq(NpmPackument.class),
		    anyMap());
		assertThat(request.getValue().getHeaders().getIfNoneMatch(), hasSize(0));
	}
	
	@Test
	public void shouldSendTheCachedEtagOnTheNextRun() throws Exception {
		stubRegistry(okWithEtag(packument(), "\"abc123\""));
		
		npm.getInfoAndVersionsFor(billingApp());
		npm.getInfoAndVersionsFor(billingApp());
		
		ArgumentCaptor<HttpEntity<Void>> request = ArgumentCaptor.captor();
		verify(restTemplate, times(2)).exchange(eq(NpmJs.REGISTRY_URL), eq(HttpMethod.GET), request.capture(),
		    eq(NpmPackument.class), anyMap());
		assertThat(request.getAllValues().get(0).getHeaders().getIfNoneMatch(), hasSize(0));
		assertThat(request.getAllValues().get(1).getHeaders().getIfNoneMatch(), contains("\"abc123\""));
	}
	
	@Test
	public void shouldReuseCachedVersionsWhenTheRegistryReportsNotModified() throws Exception {
		stubRegistry(okWithEtag(packument(), "\"abc123\""));
		npm.getInfoAndVersionsFor(billingApp());
		// a 304 carries no body at all, so the versions can only come from the cache
		stubRegistry(new ResponseEntity<>(null, HttpStatus.NOT_MODIFIED));
		
		AddOnInfoAndVersions infoAndVersions = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(infoAndVersions.getVersions(), hasSize(2));
		assertThat(infoAndVersions.getVersions().get(1).getVersion().toString(), equalTo("1.3.1"));
		assertThat(infoAndVersions.getVersions().get(1).getDownloadUri(), equalTo(TARBALL_131));
		assertThat(infoAndVersions.getVersions().get(1).getReleaseDatetime(),
		    equalTo(OffsetDateTime.parse("2026-04-14T11:02:47.512Z")));
	}
	
	@Test
	public void shouldHandFreshVersionsToEachRunSoRequirementsDoNotAccumulate() throws Exception {
		stubRegistry(okWithEtag(packument(), "\"abc123\""));
		AddOnInfoAndVersions first = npm.getInfoAndVersionsFor(billingApp());
		// the indexing run enriches the versions it is handed
		first.getVersions().get(0).addRequiredModule("webservices.rest", ">=2.24.0");
		stubRegistry(new ResponseEntity<>(null, HttpStatus.NOT_MODIFIED));
		
		AddOnInfoAndVersions second = npm.getInfoAndVersionsFor(billingApp());
		
		assertThat(second.getVersions().get(0).getRequireModules(), nullValue());
	}
	
	@Test
	public void shouldNotCacheWhenTheRegistrySendsNoEtag() throws Exception {
		stubPackument();
		npm.getInfoAndVersionsFor(billingApp());
		
		npm.getInfoAndVersionsFor(billingApp());
		
		ArgumentCaptor<HttpEntity<Void>> request = ArgumentCaptor.captor();
		verify(restTemplate, times(2)).exchange(eq(NpmJs.REGISTRY_URL), eq(HttpMethod.GET), request.capture(),
		    eq(NpmPackument.class), anyMap());
		assertThat(request.getAllValues().get(1).getHeaders().getIfNoneMatch(), hasSize(0));
	}
}
