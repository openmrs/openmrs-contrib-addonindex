package org.openmrs.addonindex.domain;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openmrs.addonindex.util.Version;

/**
 * Details about an OpenMRS add-on and its available versions
 */
public class AddOnInfoAndVersions {
	
	private String uid;
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
	private SortedSet<AddOnVersion> versions = new TreeSet<>(Comparator.reverseOrder());
	
	public static AddOnInfoAndVersions from(AddOnToIndex toIndex) {
		AddOnInfoAndVersions ret = new AddOnInfoAndVersions();
		ret.setUid(toIndex.getUid());
		ret.setName(toIndex.getName());
		ret.setDescription(toIndex.getDescription());
		ret.setType(toIndex.getType());
		return ret;
	}
	
	public void addVersion(AddOnVersion version) {
		versions.add(version);
	}
	
	public int getVersionCount() {
		return versions == null ? 0 : versions.size();
	}
	
	public Version getLatestVersion() {
		return versions == null || versions.size() == 0 ? null : versions.first().getVersion();
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
	
	public SortedSet<AddOnVersion> getVersions() {
		return versions;
	}
	
	public void setVersions(SortedSet<AddOnVersion> versions) {
		this.versions = versions;
	}
	
	public Optional<AddOnVersion> getVersion(Version version) {
		if (versions == null) {
			return null;
		}
		return versions.stream().filter(v -> {
			return v.getVersion().equals(version);
		}).findFirst();
	}
}
