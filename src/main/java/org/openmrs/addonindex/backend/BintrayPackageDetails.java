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

import com.fasterxml.jackson.annotation.JsonProperty;

public class BintrayPackageDetails {
	
	private String owner;
	
	private String repo;
	
	private String packageName; // "package" is a reserved word in Java, so we use this instead, but serialize as package
	
	public BintrayPackageDetails() {
	}
	
	public BintrayPackageDetails(String owner, String repo, String packageName) {
		this.owner = owner;
		this.repo = repo;
		this.packageName = packageName;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getRepo() {
		return repo;
	}
	
	public void setRepo(String repo) {
		this.repo = repo;
	}
	
	@JsonProperty("package")
	public String getPackageName() {
		return packageName;
	}
	
	@JsonProperty("package")
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
