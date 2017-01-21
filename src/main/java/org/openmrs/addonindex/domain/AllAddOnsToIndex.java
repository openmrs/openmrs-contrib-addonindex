package org.openmrs.addonindex.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete list of add-ons that we want to index the versions of.
 * The source of this data is a static file on github (which module authors update via pull requests).
 */
public class AllAddOnsToIndex {
	
	private List<AddOnToIndex> toIndex = new ArrayList<>();
	
	public List<AddOnToIndex> getToIndex() {
		return toIndex;
	}
	
	public void setToIndex(List<AddOnToIndex> toIndex) {
		this.toIndex = toIndex;
	}
	
	public int size() {
		return toIndex == null ? 0 : toIndex.size();
	}
}
