package org.openmrs.addonindex.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
	private String hostedUrl;
	
	private List<AddOnVersion> versions = new ArrayList<>();
	
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
		Collections.sort(versions, Comparator.reverseOrder());
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
		return versions.stream().filter(v -> {
			return v.getVersion().equals(version);
		}).findFirst();
	}
}
