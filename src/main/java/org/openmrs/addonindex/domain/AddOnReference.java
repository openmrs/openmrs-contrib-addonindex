package org.openmrs.addonindex.domain;

import org.openmrs.addonindex.util.Version;

/**
 * A reference to a specific Add On, with an optional version
 */
public class AddOnReference {
	
	private String uid;
	
	private Version version;
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public Version getVersion() {
		return version;
	}
	
	public void setVersion(Version version) {
		this.version = version;
	}
}
