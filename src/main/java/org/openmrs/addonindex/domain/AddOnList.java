package org.openmrs.addonindex.domain;

import java.util.List;

/**
 * Represents a list of add-ons, e.g. "Recommended Add-Ons" or "Included in RefApp 2.5"
 */
public class AddOnList {
	
	private String uid;
	
	private String name;
	
	private String description;
	
	private List<AddOnReference> addOns;
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
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
	
	public List<AddOnReference> getAddOns() {
		return addOns;
	}
	
	public void setAddOns(List<AddOnReference> addOns) {
		this.addOns = addOns;
	}
}
