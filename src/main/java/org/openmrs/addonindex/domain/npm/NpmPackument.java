/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.domain.npm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The document the npm registry returns for a package. "dist-tags" is deliberately not bound: we
 * want every stable version, not only the one tagged latest.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmPackument {
	
	/**
	 * The registry copies this from the latest published version's package.json.
	 */
	private String description;
	
	private Map<String, NpmVersionInfo> versions;
	
	/**
	 * Publish timestamps keyed by version string. Also contains "created" and "modified" keys, which
	 * are not versions and are simply never looked up.
	 */
	private Map<String, String> time;
	
}
