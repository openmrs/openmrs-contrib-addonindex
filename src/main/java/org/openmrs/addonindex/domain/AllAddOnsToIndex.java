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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the complete list of add-ons that we want to index the versions of.
 * The source of this data is a static file on github (which module authors update via pull requests).
 */
@Data
@NoArgsConstructor
public class AllAddOnsToIndex {
	
	private List<AddOnToIndex> toIndex = new ArrayList<>();
	
	private List<AddOnList> lists = new ArrayList<>();
	
	public int size() {
		return toIndex == null ? 0 : toIndex.size();
	}
	
	public Optional<AddOnToIndex> getAddOnByUid(String uid) {
		return toIndex.stream().filter(addOn -> addOn.getUid().equals(uid)).findFirst();
	}
	
	public Optional<AddOnList> getListByUid(String uid) {
		return lists.stream().filter(addOn -> addOn.getUid().equals(uid)).findFirst();
	}
	
}
