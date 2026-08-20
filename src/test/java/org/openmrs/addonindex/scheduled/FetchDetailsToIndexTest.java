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

import java.io.IOException;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.backend.SupportsVersionDetails;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.Version;
import org.springframework.web.client.RestClientException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

public class FetchDetailsToIndexTest {
	
	private AddOnToIndex frontendAppToIndex() {
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("openmrs-esm-billing-app");
		toIndex.setType(AddOnType.FRONTEND_MODULE);
		return toIndex;
	}
	
	private AddOnVersion versionOf(String versionString) {
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version(versionString));
		return version;
	}
	
	private AddOnInfoAndVersions infoWith(AddOnVersion... versions) {
		AddOnInfoAndVersions infoAndVersions = new AddOnInfoAndVersions();
		for (AddOnVersion version : versions) {
			infoAndVersions.getVersions().add(version);
		}
		return infoAndVersions;
	}
	
	@Test
	public void shouldDropAVersionWhoseDetailFetchFailsSoALaterRunRetriesIt() throws Exception {
		IndexingService indexingService = mock(IndexingService.class);
		FetchDetailsToIndex task = new FetchDetailsToIndex(indexingService, null);
		
		AddOnToIndex toIndex = frontendAppToIndex();
		AddOnVersion failing = versionOf("1.3.1");
		AddOnInfoAndVersions infoAndVersions = infoWith(versionOf("1.3.0"), failing);
		
		SupportsVersionDetails handler = mock(SupportsVersionDetails.class);
		// a version indexed without its details would never be fetched again: the reuse check
		// compares downloadUri and releaseDatetime, and neither ever changes for an npm version
		doThrow(new RestClientException("503 from the registry")).when(handler).fetchVersionDetails(any(), same(failing));
		
		task.fetchExtraDetailsForEachVersion(handler, toIndex, infoAndVersions);
		
		assertThat(infoAndVersions.getVersions(), hasSize(1));
		assertThat(infoAndVersions.getVersions().get(0).getVersion().toString(), is("1.3.0"));
	}
	
	@Test
	public void shouldNotIndexAnythingWhenEveryVersionsDetailFetchFails() throws Exception {
		IndexingService indexingService = mock(IndexingService.class);
		when(indexingService.getIndexingStatus()).thenReturn(new IndexingStatus());
		AddOnToIndex toIndex = frontendAppToIndex();
		BackendHandler handler = mock(BackendHandler.class, withSettings().extraInterfaces(SupportsVersionDetails.class));
		when(indexingService.getHandlerFor(toIndex)).thenReturn(handler);
		when(handler.getInfoAndVersionsFor(toIndex)).thenReturn(infoWith(versionOf("1.3.0")));
		doThrow(new RestClientException("registry outage")).when((SupportsVersionDetails) handler).fetchVersionDetails(any(),
		    any());
		FetchDetailsToIndex task = new FetchDetailsToIndex(indexingService, null);
		
		task.getDetailsAndIndex(toIndex);
		
		// the guard in fetchExtraDetailsForEachVersion must reach getDetailsAndIndex's catch:
		// the run records an error and never replaces the document with an empty one
		verify(indexingService, never()).index(any());
		assertThat(indexingService.getIndexingStatus().getStatuses().get("openmrs-esm-billing-app").getError(),
		    notNullValue());
	}
	
	@Test
	public void shouldFailTheRunWhenEveryVersionsDetailFetchFails() throws Exception {
		IndexingService indexingService = mock(IndexingService.class);
		FetchDetailsToIndex task = new FetchDetailsToIndex(indexingService, null);
		
		AddOnToIndex toIndex = frontendAppToIndex();
		AddOnInfoAndVersions infoAndVersions = infoWith(versionOf("1.3.0"));
		
		SupportsVersionDetails handler = mock(SupportsVersionDetails.class);
		doThrow(new RestClientException("registry outage")).when(handler).fetchVersionDetails(any(), any());
		
		// dropping every version must not index an empty document as a success
		assertThrows(IllegalStateException.class,
		    () -> task.fetchExtraDetailsForEachVersion(handler, toIndex, infoAndVersions));
	}
	
	@Test
	public void shouldReuseAnIndexedVersionAndSkipItsDetailFetch() throws Exception {
		IndexingService indexingService = mock(IndexingService.class);
		FetchDetailsToIndex task = new FetchDetailsToIndex(indexingService, null);
		
		AddOnToIndex toIndex = frontendAppToIndex();
		
		// real npm versions always carry a release date, so this exercises the
		// datetime-equality branch of the reuse check, the one production takes
		OffsetDateTime released = OffsetDateTime.parse("2026-04-14T11:02:47Z");
		AddOnVersion incoming = versionOf("1.3.0");
		incoming.setDownloadUri("https://registry.npmjs.org/esm-billing-app-1.3.0.tgz");
		incoming.setReleaseDatetime(released);
		
		AddOnVersion indexed = versionOf("1.3.0");
		indexed.setDownloadUri("https://registry.npmjs.org/esm-billing-app-1.3.0.tgz");
		indexed.setReleaseDatetime(released);
		indexed.addRequiredModule("webservices.rest", ">=2.24.0");
		AddOnInfoAndVersions existing = new AddOnInfoAndVersions();
		existing.getVersions().add(indexed);
		when(indexingService.getByUid("openmrs-esm-billing-app")).thenReturn(existing);
		
		AddOnInfoAndVersions infoAndVersions = new AddOnInfoAndVersions();
		infoAndVersions.getVersions().add(incoming);
		
		SupportsVersionDetails handler = mock(SupportsVersionDetails.class);
		
		task.fetchExtraDetailsForEachVersion(handler, toIndex, infoAndVersions);
		
		// the reuse check is what bounds tarball downloads to versions the index does not hold yet
		verify(handler, never()).fetchVersionDetails(any(), any());
		assertThat(infoAndVersions.getVersions().get(0), sameInstance(indexed));
	}
	
	@Test
	public void shouldKeepAnOmodVersionWhoseConfigXmlFetchFails() throws Exception {
		IndexingService indexingService = mock(IndexingService.class);
		FetchDetailsToIndex task = spy(new FetchDetailsToIndex(indexingService, null));
		doThrow(new IOException("artifactory outage")).when(task).fetchZipEntry(any(), eq("config.xml"));
		
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("reporting");
		toIndex.setType(AddOnType.OMOD);
		
		AddOnInfoAndVersions infoAndVersions = infoWith(versionOf("1.0.0"));
		
		task.fetchExtraDetailsForEachVersion(mock(BackendHandler.class), toIndex, infoAndVersions);
		
		// long-standing OMOD behavior: a version whose config.xml fetch fails is indexed without
		// details, never dropped. Only SupportsVersionDetails versions get the drop-and-retry.
		assertThat(infoAndVersions.getVersions(), hasSize(1));
	}
	
	@Test
	public void testParsingConfigXmlForLanguages() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		AddOnInfoAndVersions addOnInfoAndVersions = new AddOnInfoAndVersions();
		task.handleConfigXml(getFileAsString("config.withNoRequirements.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), nullValue());
		assertThat(version.getRequireModules(), nullValue());
		assertThat(version.getSupportedLanguages(), contains("en", "fr", "de"));
	}
	
	@Test
	public void testParsingConfigXmlForRequiredOpenmrsVersion() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		AddOnInfoAndVersions addOnInfoAndVersions = new AddOnInfoAndVersions();
		task.handleConfigXml(getFileAsString("config.withRequiredVersion.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("1.11.3, 1.10.2 - 1.10.*, 1.9.9 - 1.9.*"));
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void testParsingConfigXmlForRequiredModuleVersion() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		AddOnInfoAndVersions addOnInfoAndVersions = new AddOnInfoAndVersions();
		task.handleConfigXml(getFileAsString("config.withRequiredModules.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("1.11.3, 1.10.2 - 1.10.*, 1.9.9 - 1.9.*"));
		assertThat(version.getRequireModules().size(), is(2));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.module.reporting")), hasProperty("version", is("?")))));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.event")), hasProperty("version", is("?")))));
	}
	
	@Test
	public void testParsingConfigXmlForSettingModulePackageAndId() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		task.handleConfigXml(getFileAsString("config.withRelativePathDtd.xml"), version);
		assertThat(version.getModulePackage(), is("org.openmrs.module.mdrtb"));
		assertThat(version.getModuleId(), is("mdrtb"));
	}
	
	@Test
	public void testParsingWithDoctypeRelativePath() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		AddOnInfoAndVersions addOnInfoAndVersions = new AddOnInfoAndVersions();
		task.handleConfigXml(getFileAsString("config.withRelativePathDtd.xml"), version);
		// just test that we could parse at all
	}
	
	@Test
	public void testParsingWithDoctypeRelativePathOnTwoLines() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		task.handleConfigXml(getFileAsString("config.withRelativePathDtdOnTwoLines.xml"), version);
		// just test that we could parse at all
	}
	
	@Test
	public void testParsingWithCommentedDoctype() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		AddOnInfoAndVersions addOnInfoAndVersions = new AddOnInfoAndVersions();
		task.handleConfigXml(getFileAsString("config.withCommentedDoctype.xml"), version);
		// just test that we could parse at all
	}
	
	@Test
	public void testParsingContentPropertiesForDependencies() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		task.handleContentProperties(getFileAsString("content.withDependencies.properties"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("2.4.0"));
		// name, version, the .groupId and .type sub-keys, and the var.* config values are all excluded
		assertThat(version.getRequireModules().size(), is(7));
		// omod.* requirements are recorded under the module package they are indexed by
		assertThat(version.getRequireModules(), hasItem(
		    allOf(hasProperty("module", is("org.openmrs.module.webservices.rest")), hasProperty("version", is("2.44.0")))));
		// ...which for the event module means honouring the groupId it declares, rather than assuming one
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.event")), hasProperty("version", is("2.0.0 - 2.*")))));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.owa.addonmanager")), hasProperty("version", is("1.2.0")))));
		// frontend modules aren't indexed here, so the npm package name is the only identifier they have
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("@openmrs/esm-generic-patient-widgets-app")),
		        hasProperty("version", is("7.0.0 - 7.*")))));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.content.referenceapplication")),
		        hasProperty("version", is("1.5.0")))));
		// a requirement whose version we can't use is still recorded, with an unknown version
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.module.appui")), hasProperty("version", is("?")))));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.module.legacyui")), hasProperty("version", is("?")))));
	}
	
	@Test
	public void testParsingContentPropertiesIgnoresUnrecognizedKeys() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		task.handleContentProperties("foo.bar=1.0\nsomething=1.0\n", version);
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void testParsingContentPropertiesFallsBackToDefaultGroupId() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		task.handleContentProperties("omod.foo=1.0\nomod.foo.groupId=${someGroupId}\nowa.bar=1.0\nowa.bar.groupId=\n",
		    version);
		assertThat(version.getRequireModules().size(), is(2));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.module.foo")), hasProperty("version", is("1.0")))));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.owa.bar")), hasProperty("version", is("1.0")))));
	}
	
	@Test
	public void testParsingContentPropertiesWithOnlyMetadata() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex(null, null);
		AddOnVersion version = new AddOnVersion();
		task.handleContentProperties(getFileAsString("content.nameVersionOnly.properties"), version);
		assertThat(version.getRequireOpenmrsVersion(), nullValue());
		assertThat(version.getRequireModules(), nullValue());
	}
}
