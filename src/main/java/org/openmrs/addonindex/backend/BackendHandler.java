package org.openmrs.addonindex.backend;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;

/**
 * Represents a Strategy for fetching details and available versions of an Add-On that we want to index.
 * Implementations will typically make one or more HTTP calls to get this info.
 */
public interface BackendHandler {
	
	AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception;
	
}
