/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.backend;

import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;

/**
 * This interface indicates that a BackendHandler implementation is able to fetch details about a
 * single version that its version listing does not already include, e.g. the modules that version
 * requires. Where those details live is specific to the back end, so the implementation is
 * responsible for fetching them and setting them on the AddOnVersion.
 */
public interface SupportsVersionDetails extends BackendHandler {
	
	/**
	 * Fetches one version's details and sets whatever it finds on addOnVersion. Implementations will
	 * typically make an HTTP call per version, so the caller (see
	 * FetchDetailsToIndex#fetchExtraDetailsForEachVersion) skips versions whose indexed data is already
	 * current. If this throws, the caller leaves the version out of the run entirely, so a later run
	 * retries it instead of indexing it with incomplete details.
	 */
	void fetchVersionDetails(AddOnToIndex toIndex, AddOnVersion addOnVersion) throws Exception;
}
