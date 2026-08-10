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

import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnVersion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

public class FetchDetailsToIndexTest {
	
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
		    allOf(hasProperty("module", is("org.openmrs.module.webservices.rest")), hasProperty("version", is("2.44")))));
		// ...which for the event module means honouring the groupId it declares, rather than assuming one
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.event")), hasProperty("version", is("2.*")))));
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("org.openmrs.owa.addonmanager")), hasProperty("version", is("1.2.0")))));
		// frontend modules aren't indexed here, so the npm package name is the only identifier they have
		assertThat(version.getRequireModules(),
		    hasItem(allOf(hasProperty("module", is("@openmrs/esm-generic-patient-widgets-app")),
		        hasProperty("version", is("7.*")))));
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
