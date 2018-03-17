package org.openmrs.addonindex.backend;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;

/**
 *Represents a Strategy for fetching number of downloads over the past 30 days.
 * Implementations will typically make one or more HTTP calls to get this info
 */
public interface SupportsDownloadCounts extends BackendHandler {

    /**
     * Implementations should query/fetch from their backend and get the number of downloads of a particular
     * AddOn over the past 30 days
     *
     * @param toIndex
     * @param infoAndVersions
     * @return
     * @throws Exception
     */
    Integer fetchDownloadCounts(AddOnToIndex toIndex, AddOnInfoAndVersions infoAndVersions) throws Exception;
}
