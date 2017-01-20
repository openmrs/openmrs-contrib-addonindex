package org.openmrs.addonindex.domain;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Details about an OpenMRS add-on and its available versions
 */
public class AddOnInfoAndVersions {
	
	private String uid;
	
	private AddOnType type;
	
	private String name;
	
	private SortedSet<AddOnVersion> versions;
	
	public static AddOnInfoAndVersions from(AddOnToIndex toIndex) {
		AddOnInfoAndVersions ret = new AddOnInfoAndVersions();
		ret.setUid(toIndex.getUid());
		ret.setName(toIndex.getName());
		ret.setType(toIndex.getType());
		return ret;
	}
	
	public void addVersion(AddOnVersion version) {
		if (versions == null) {
			versions = new TreeSet<>();
		}
		versions.add(version);
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
	
	public SortedSet<AddOnVersion> getVersions() {
		return versions;
	}
	
	public void setVersions(SortedSet<AddOnVersion> versions) {
		this.versions = versions;
	}
}
