package org.openmrs.addonindex.backend;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;

/**
 * This interface indicates that a BackendHandler implementation is able to fetch download counts from its back end.
 * Different back ends may report different groupings of download counts, so implementations
 * are allowed to store any sort of download count data they want on the AddOnInfoAndVersions object.
 */
public interface SupportsDownloadCounts extends BackendHandler {

    /**
     * Represents a Strategy for fetching download counts.
     * Implementations will typically make one or more HTTP calls to get this info
     * The implementation is responsible for setting the download count data on infoAndVersions.
     * (Different backends may report download counts with different groupings/timeframes,
     * so we do not specify a specific field to set this in.)
     *
     * @param toIndex
     * @param infoAndVersions
     * @return
     */
    void fetchDownloadCounts(AddOnToIndex toIndex, AddOnInfoAndVersions infoAndVersions);
}
