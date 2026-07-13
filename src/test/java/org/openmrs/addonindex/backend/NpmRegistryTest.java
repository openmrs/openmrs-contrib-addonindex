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
import org.openmrs.addonindex.domain.backend.NpmPackageDetails;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NpmRegistryTest {
	
	private AddOnToIndex toIndex() {
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("openmrs-esm-patient-chart");
		toIndex.setName("Patient Chart");
		toIndex.setType(AddOnType.FRONTEND_MODULE);
		NpmPackageDetails details = new NpmPackageDetails();
		details.setPackageName("@openmrs/esm-patient-chart-app");
		toIndex.setNpmPackageDetails(details);
		return toIndex;
	}
	
	@Test
	public void excludesPreReleasesAndMapsFields() throws Exception {
		String json = TestUtil.getFileAsString("npmRegistryResponse.json");
		NpmRegistry backend = new NpmRegistry(null, new ObjectMapper());
		AddOnInfoAndVersions result = backend.handleRegistryJson(toIndex(), json);
		
		List<AddOnVersion> versions = result.getVersions();
		assertThat(versions.size(), is(2)); // pre-release excluded
		// sorted descending by addVersion()
		AddOnVersion latest = versions.get(0);
		assertThat(latest.getVersion().toString(), is("9.0.1"));
		assertThat(latest.getDownloadUri(),
		    is("https://registry.npmjs.org/@openmrs/esm-patient-chart-app/-/esm-patient-chart-app-9.0.1.tgz"));
		assertThat(result.getHostedUrl(), is("https://www.npmjs.com/package/@openmrs/esm-patient-chart-app"));
	}
	
	@Test
	public void parsesDownloadCount() throws Exception {
		String json = TestUtil.getFileAsString("npmDownloadCounts.json");
		NpmRegistry backend = new NpmRegistry(null, new ObjectMapper());
		assertThat(backend.parseDownloadCount(json), is(12408));
	}
}
