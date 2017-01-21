package org.openmrs.addonindex.domain;

import org.openmrs.addonindex.util.Version;
import org.springframework.beans.BeanUtils;

/**
 * A summary of an Add On and its Versions that we have indexed, e.g. for use as a search result
 */
public class AddOnInfoSummary {
	
	private String uid;
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
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
