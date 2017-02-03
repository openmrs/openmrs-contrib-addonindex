package org.openmrs.addonindex.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of fetching actual details of the addons in an {@link AddOnList}
 */
public class MaterializedAddOnList {
	
	private String uid;
	
	private String name;
	
	private String description;
	
	private List<MaterializedReference> addOns;
	
	public MaterializedAddOnList(AddOnList list) {
		this.uid = list.getUid();
		this.name = list.getName();
		this.description = list.getDescription();
		addOns = new ArrayList<>();
	}
	
	public void add(MaterializedReference materializedReference) {
		addOns.add(materializedReference);
	}
	
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
	
	public List<MaterializedReference> getAddOns() {
		return addOns;
	}
	
	public void setAddOns(List<MaterializedReference> addOns) {
		this.addOns = addOns;
	}
}
