package org.openmrs.addonindex.service;

import java.util.Collection;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;

/**
 * Interface for accessing the datastore where we have indexed information about add-ons and versions.
 */
public interface Index {
	
	void index(AddOnInfoAndVersions infoAndVersions) throws Exception;
	
	Collection<AddOnInfoSummary> search(AddOnType type, String query) throws Exception;
	
	Collection<AddOnInfoAndVersions> getAllByType(AddOnType type) throws Exception;
	
	AddOnInfoAndVersions getByUid(String uid) throws Exception;
}
