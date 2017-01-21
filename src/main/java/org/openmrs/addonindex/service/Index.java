package org.openmrs.addonindex.service;

import java.util.Collection;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;

public interface Index {
	
	void index(AddOnInfoAndVersions infoAndVersions);
	
	Collection<AddOnInfoSummary> search(AddOnType type, String query);
	
	Collection<AddOnInfoAndVersions> getAllByType(AddOnType type);
	
	AddOnInfoAndVersions getByUid(String uid);
}
