/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.service;

import java.util.Collection;
import java.util.List;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.openmrs.addonindex.domain.AddOnType;

/**
 * Interface for accessing the datastore where we have indexed information about add-ons and versions.
 */
public interface Index {
	
	void index(AddOnInfoAndVersions infoAndVersions) throws Exception;
	
	Collection<AddOnInfoSummary> search(AddOnType type, String query, String tag) throws Exception;
	
	Collection<AddOnInfoAndVersions> getAllByType(AddOnType type) throws Exception;
	
	Collection<AddOnInfoAndVersions> getByTag(String tag) throws Exception;

	AddOnInfoAndVersions getByModulePackage(String modulePackage) throws Exception;
	
	AddOnInfoAndVersions getByUid(String uid) throws Exception;
	
	List<AddOnInfoSummaryAndStats> getTopDownloaded() throws Exception;
}
