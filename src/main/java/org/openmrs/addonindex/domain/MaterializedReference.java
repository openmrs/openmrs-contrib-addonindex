package org.openmrs.addonindex.domain;

import org.openmrs.addonindex.util.Version;

/**
 * Represents the result of getting a Module and/or Version for a Reference
 */
public class MaterializedReference {
	
	private String uid;
	
	private Version version;
	
	private AddOnInfoSummary details;
	
	private AddOnVersion addOnVersion;
	
	public MaterializedReference(AddOnReference addOnReference) {
		this.uid = addOnReference.getUid();
		this.version = addOnReference.getVersion();
	}
	
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
	
	public AddOnInfoSummary getDetails() {
		return details;
	}
	
	public void setDetails(AddOnInfoSummary details) {
		this.details = details;
	}
	
	public AddOnVersion getAddOnVersion() {
		return addOnVersion;
	}
	
	public void setAddOnVersion(AddOnVersion addOnVersion) {
		this.addOnVersion = addOnVersion;
	}
}
