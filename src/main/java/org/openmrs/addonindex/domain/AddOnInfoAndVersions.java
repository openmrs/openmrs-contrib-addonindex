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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.openmrs.addonindex.util.Version;

import io.searchbox.annotations.JestId;

/**
 * Details about an OpenMRS add-on and its available versions
 */
public class AddOnInfoAndVersions {
	
	public final static String ES_INDEX = "add_on_info_and_versions";
	
	public final static String ES_TYPE = "add_on_info_and_versions";
	
	@JestId
	private String uid;

	private String modulePackage;

	private String moduleId;

	private AddOnStatus status;
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
	private String icon;
	
	private List<String> tags;
	
	private List<Maintainer> maintainers;
	
	private String hostedUrl;
	
	private List<AddOnVersion> versions = new ArrayList<>();
	
	public static AddOnInfoAndVersions from(AddOnToIndex toIndex) {
		AddOnInfoAndVersions ret = new AddOnInfoAndVersions();
		ret.setUid(toIndex.getUid());
		ret.setStatus(toIndex.getStatus());
		ret.setName(toIndex.getName());
		ret.setDescription(toIndex.getDescription());
		ret.setIcon(toIndex.getIcon());
		ret.setTags(toIndex.getTags());
		ret.setType(toIndex.getType());
		ret.setMaintainers(toIndex.getMaintainers());
		return ret;
	}
	
	public void addVersion(AddOnVersion version) {
		versions.add(version);
		versions.sort(Comparator.reverseOrder());
	}
	
	public int getVersionCount() {
		return versions == null ? 0 : versions.size();
	}
	
	public Version getLatestVersion() {
		return versions == null || versions.size() == 0 ? null : versions.get(0).getVersion();
	}
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getModulePackage() {
		return modulePackage;
	}

	public void setModulePackage(String modulePackage) {
		this.modulePackage = modulePackage;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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
	
	public List<Maintainer> getMaintainers() {
		return maintainers;
	}
	
	public void setMaintainers(List<Maintainer> maintainers) {
		this.maintainers = maintainers;
	}
	
	public String getHostedUrl() {
		return hostedUrl;
	}
	
	public void setHostedUrl(String hostedUrl) {
		this.hostedUrl = hostedUrl;
	}
	
	public List<AddOnVersion> getVersions() {
		return versions;
	}
	
	public void setVersions(List<AddOnVersion> versions) {
		this.versions = versions;
	}
	
	public Optional<AddOnVersion> getVersion(Version version) {
		if (versions == null) {
			return null;
		}
		return versions.stream().filter(v -> v.getVersion().equals(version)).findFirst();
	}
	
	public void addTag(String tag) {
		if (!Pattern.matches("[^\\s]*", tag)) {
			throw new IllegalArgumentException("Tag cannot contain whitespace:" + tag);
		}
		if (tags == null) {
			tags = new ArrayList<>();
		}
		tags.add(tag);
	}
	
	public void setDetailsBasedOnLatestVersion() {
		Optional<AddOnVersion> version = getVersion(getLatestVersion());
		if (version.isPresent()) {
			setModuleId(version.get().getModuleId());
			setModulePackage(version.get().getModulePackage());
		}
	}
}
