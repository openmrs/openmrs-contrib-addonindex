/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.legacy;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;

public class LegacyFindModulesResponse {
	
	private Integer sEcho;
	
	private Integer iTotalRecords;
	
	private List<String[]> aaData = new ArrayList<>();
	
	@JsonGetter("sColumns")
	public String getsColumns() {
		return "Action,Name,Version,Author,Description";
	}
	
	public void addRow(String url, String name, String version, String owner, String description) {
		aaData.add(new String[] { url, name, version, owner, description });
	}
	
	public Integer getsEcho() {
		return sEcho;
	}
	
	public void setsEcho(Integer sEcho) {
		this.sEcho = sEcho;
	}
	
	public Integer getiTotalRecords() {
		return iTotalRecords;
	}
	
	public void setiTotalRecords(Integer iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}
	
	public Integer getiTotalDisplayRecords() {
		return aaData.size();
	}
	
	public List<String[]> getAaData() {
		return aaData;
	}
	
	public void setAaData(List<String[]> aaData) {
		this.aaData = aaData;
	}
}
