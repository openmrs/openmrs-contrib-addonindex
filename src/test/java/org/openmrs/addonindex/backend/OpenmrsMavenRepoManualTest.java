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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * This test makes an HTTP call to the OpenMRS maven repo. Enable this if you want to do manual
 * troubleshooting.
 */
@Disabled
@SpringBootTest
public class OpenmrsMavenRepoManualTest {
	
	@Autowired
	private OpenmrsMavenRepo backend;
	
	@Test
	public void manualTestMakingWebCall() throws Exception {
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("appui-module");
		toIndex.setName("App UI Module");
		toIndex.setType(AddOnType.OMOD);
		toIndex.setMavenRepoDetails(new MavenRepoDetails("org.openmrs.module", "appui-omod"));
		
		AddOnInfoAndVersions versionsFor = backend.getInfoAndVersionsFor(toIndex);
		
		System.out.println(versionsFor.getName());
		System.out.println(versionsFor.getVersions().get(0).getDownloadUri());
		assertThat(versionsFor.getName(), is("App UI Module"));
		assertThat(versionsFor.getVersions().get(0).getVersion().toString(), is("1.0"));
		assertThat(versionsFor.getVersions().get(0).getRequireOpenmrsVersion(), is("1.9.1"));
	}
	
}
