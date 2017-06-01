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

import org.openmrs.addonindex.util.Version;
import org.springframework.beans.BeanUtils;

/**
 * A summary of an Add On and its Versions that we have indexed, e.g. for use as a search result
 */
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
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public AddOnStatus getStatus() {
		return status;
	}
	
	public void setStatus(AddOnStatus status) {
		this.status = status;
	}
	
	public AddOnType getType() {
		return type;
	}
	
	public void setType(AddOnType type) {
		this.type = type;
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
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public int getVersionCount() {
		return versionCount;
	}
	
	public void setVersionCount(int versionCount) {
		this.versionCount = versionCount;
	}
	
	public Version getLatestVersion() {
		return latestVersion;
	}
	
	public void setLatestVersion(Version latestVersion) {
		this.latestVersion = latestVersion;
	}
}
