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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.BeanUtils;

/**
 * A summary of an Add On and its Versions that we have indexed, e.g. for use as a search result
 */
@Data
@NoArgsConstructor
public class AddOnInfoSummary {
	
	private String uid;
	
	private AddOnStatus status;
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
	private String icon;
	
	private List<String> tags;
	
	private int versionCount;
	
	private Version latestVersion;
	
	public AddOnInfoSummary(AddOnInfoAndVersions full) {
		BeanUtils.copyProperties(full, this);
	}

}
