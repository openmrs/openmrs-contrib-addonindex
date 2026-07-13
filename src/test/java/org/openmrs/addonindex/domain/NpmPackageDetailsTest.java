/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.domain;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NpmPackageDetailsTest {
	
	@Test
	public void deserializesFromAddOnToIndexJson() throws Exception {
		String json = "{ \"uid\": \"x\", \"type\": \"FRONTEND_MODULE\", "
		        + "\"npmPackageDetails\": { \"packageName\": \"@openmrs/esm-patient-chart-app\" } }";
		AddOnToIndex toIndex = new ObjectMapper().readValue(json, AddOnToIndex.class);
		assertThat(toIndex.getNpmPackageDetails().getPackageName(), is("@openmrs/esm-patient-chart-app"));
	}
}
