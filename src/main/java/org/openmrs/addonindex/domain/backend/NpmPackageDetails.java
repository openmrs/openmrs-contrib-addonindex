/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.domain.backend;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Where to find a FRONTEND_MODULE on an NPM registry. The uid cannot hold the package name directly
 * because a scoped name like "@openmrs/esm-billing-app" does not survive being a single URL path
 * segment.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class NpmPackageDetails {
	
	@NonNull
	private String packageName;
	
}
