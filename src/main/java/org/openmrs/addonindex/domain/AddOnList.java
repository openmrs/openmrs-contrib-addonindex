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

/**
 * Represents a list of add-ons, e.g. "Recommended Add-Ons" or "Included in RefApp 2.5"
 */
public class AddOnList {
	
	private String uid;
	
	private String name;
	
	private String description;
	
	private List<AddOnReference> addOns;
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<AddOnReference> getAddOns() {
		return addOns;
	}
	
	public void setAddOns(List<AddOnReference> addOns) {
		this.addOns = addOns;
	}
}
