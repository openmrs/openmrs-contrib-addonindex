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
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MavenContentPackageTest {
	
	@Test
	public void indexesReleaseVersionsWithZipUris() throws Exception {
		String xml = TestUtil.getFileAsString("contentPackageMavenMetadata.xml");
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("org.openmrs.content.referenceapplication");
		toIndex.setType(AddOnType.CONTENT_PACKAGE);
		MavenRepoDetails details = new MavenRepoDetails();
		details.setGroupId("org.openmrs.content");
		details.setArtifactId("referenceapplication");
		toIndex.setMavenRepoDetails(details);
		
		AddOnInfoAndVersions result = new MavenContentPackage(null).handleMavenMetadata(toIndex, xml);
		
		List<AddOnVersion> versions = result.getVersions();
		assertThat(versions.size(), is(2)); // SNAPSHOT excluded
		AddOnVersion latest = versions.get(0);
		assertThat(latest.getVersion().toString(), is("1.4.0"));
		assertThat(latest.getDownloadUri(), is("https://mavenrepo.openmrs.org/nexus/content/repositories/public/"
		        + "org/openmrs/content/referenceapplication/1.4.0/referenceapplication-1.4.0.zip"));
	}
}
