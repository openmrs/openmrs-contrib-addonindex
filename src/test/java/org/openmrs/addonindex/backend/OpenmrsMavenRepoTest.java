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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.TestUtil;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OpenmrsMavenRepoTest {
	
	@Test
	public void testHandlingNexusXml() throws Exception {
		String xml = TestUtil.getFileAsString("indexBrowserTreeViewResponse.xml");
		
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("appui-module");
		toIndex.setName("App UI Module");
		toIndex.setDescription("For apps");
		toIndex.setType(AddOnType.OMOD);
		
		OpenmrsMavenRepo backend = new OpenmrsMavenRepo(null);
		AddOnInfoAndVersions addOnInfoAndVersions = backend.handleIndexBrowserTreeViewResponse(toIndex, xml);
		
		assertThat(addOnInfoAndVersions.getUid(), is(toIndex.getUid()));
		assertThat(addOnInfoAndVersions.getName(), is(toIndex.getName()));
		assertThat(addOnInfoAndVersions.getDescription(), is(toIndex.getDescription()));
		assertThat(addOnInfoAndVersions.getType(), is(toIndex.getType()));
		
		List<AddOnVersion> versions = addOnInfoAndVersions.getVersions();
		assertThat(versions.size(), is(10));
		
		assertThat(versions.get(versions.size() - 1).getVersion().toString(), is("1.0"));
		assertThat(versions.get(versions.size() - 1).getDownloadUri(),
		    is("https://mavenrepo.openmrs.org/nexus/service/local/repositories/modules/content/org/openmrs/module/appui"
		            + "-omod/1.0/appui-omod-1.0.jar"));
		assertThat(versions.get(versions.size() - 1).getRenameTo(), is("appui-1.0.omod"));
		
		assertThat(versions.get(0).getVersion().toString(), is("1.7"));
		assertThat(versions.get(0).getDownloadUri(),
		    is("https://mavenrepo.openmrs.org/nexus/service/local/repositories/modules/content/org/openmrs/module/appui"
		            + "-omod/1.7/appui-omod-1.7.jar"));
		assertThat(versions.get(0).getRenameTo(), is("appui-1.7.omod"));
	}
}
