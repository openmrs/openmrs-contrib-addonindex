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

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;

/**
 * Represents a Strategy for fetching details and available versions of an Add-On that we want to index.
 * Implementations will typically make one or more HTTP calls to get this info.
 */
public interface BackendHandler {
	
	/**
	 * Implementations should query/fetch from their backend and get readily-available information about the add-on, what
	 * versions are available, and what URIs to download them from.
	 * They should _not_ fetch and parse the omod files themselves (e.g. to determine required OpenMRS version), as
	 * this will be taken care of elsewhere in the application.
	 *
	 * @param addOnToIndex
	 * @return
	 * @throws Exception
	 */
	AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception;
	
}
