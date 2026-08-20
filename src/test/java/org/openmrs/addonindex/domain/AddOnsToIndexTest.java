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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.backend.NpmJs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

@JsonTest
class AddOnsToIndexTest {
	
	private static final Pattern URL_SAFE_UID = Pattern.compile("[a-z0-9._-]+");
	
	// Jackson cannot enforce @NonNull when the key is simply absent, and a typo in a package name
	// only fails much later at the registry, so the catalogue is checked here instead
	private static final Pattern NPM_PACKAGE_NAME = Pattern.compile("^(@[a-z0-9._-]+/)?[a-z0-9._-]+$");
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private List<AddOnToIndex> frontendModules() throws Exception {
		AllAddOnsToIndex all = objectMapper.readValue(getFileAsString("add-ons-to-index.json"), AllAddOnsToIndex.class);
		return all.getToIndex().stream().filter(a -> a.getType() == AddOnType.FRONTEND_MODULE).collect(Collectors.toList());
	}
	
	@Test
	public void shouldRegisterEveryReferenceApplicationFrontendModule() throws Exception {
		// 45 is every app in the reference application's spa-assemble-config.json except
		// @openmrs/esm-patient-growth-chart-app (which has no stable npm release); update this
		// number when that config changes
		assertThat(frontendModules(), hasSize(45));
	}
	
	@Test
	public void everyFrontendModuleShouldDeclareAnNpmPackageAndTheNpmBackend() throws Exception {
		for (AddOnToIndex addOn : frontendModules()) {
			assertThat("npmPackageDetails missing for " + addOn.getUid(), addOn.getNpmPackageDetails(), notNullValue());
			String packageName = addOn.getNpmPackageDetails().getPackageName();
			assertThat("packageName is not a valid npm name for " + addOn.getUid(),
			    packageName != null && NPM_PACKAGE_NAME.matcher(packageName).matches(), equalTo(true));
			assertThat("name should be the package name for " + addOn.getUid(), addOn.getName(), equalTo(packageName));
			assertThat("wrong backend for " + addOn.getUid(), addOn.getBackend(), equalTo(NpmJs.class));
		}
	}
	
	@Test
	public void everyFrontendModuleUidShouldBeTheUrlSafePackageName() throws Exception {
		for (AddOnToIndex addOn : frontendModules()) {
			// uid is a single URL path segment in /api/v1/addon/{uid} and /show/:uid
			assertThat("uid is not URL-safe: " + addOn.getUid(), URL_SAFE_UID.matcher(addOn.getUid()).matches(),
			    equalTo(true));
			String expected = addOn.getNpmPackageDetails().getPackageName().replace("@", "").replace("/", "-");
			assertThat(addOn.getUid(), equalTo(expected));
		}
	}
	
	@Test
	public void everyFrontendModuleShouldLinkToItsSource() throws Exception {
		for (AddOnToIndex addOn : frontendModules()) {
			boolean hasSource = addOn.getLinks() != null && addOn.getLinks().stream()
			        .anyMatch(l -> "source".equals(l.getRel()) && l.getHref().startsWith("https://github.com/openmrs/"));
			assertThat("no source link for " + addOn.getUid(), hasSource, equalTo(true));
		}
	}
}
