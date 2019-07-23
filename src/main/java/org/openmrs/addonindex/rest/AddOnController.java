/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.rest;

import java.util.Collection;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddOnController {
	
	private Index index;
	
	@Autowired
	public AddOnController(Index index) {
		this.index = index;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon")
	public Collection<AddOnInfoSummary> search(@RequestParam(value = "type", required = false) AddOnType type,
	                                           @RequestParam(value = "q", required = false) String query, 
						   @RequestParam(value = "tag", required = false) String tag) throws Exception {
		return index.search(type, query, tag);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon", params = "modulePackage")
	public ResponseEntity<AddOnInfoAndVersions> getByModulePackage(@RequestParam(value = "modulePackage") String modulePackage) throws Exception {
		AddOnInfoAndVersions addOn = index.getByModulePackage(modulePackage);
		return new ResponseEntity<>(addOn, addOn == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon/{uid}")
	public ResponseEntity<AddOnInfoAndVersions> getOne(@PathVariable String uid) throws Exception {
		AddOnInfoAndVersions addOn = index.getByUid(uid);
		return new ResponseEntity<>(addOn, addOn == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
	}

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon/{uid}/latestVersion")
    public ResponseEntity<AddOnVersion> getLatestVersion(
            @RequestParam(value = "coreversion", required = false) String userCoreVersion,
            @PathVariable String uid) throws Exception {
	    AddOnVersion addOnVersion = index.getByUid(uid).getLatestSupportedVersion(userCoreVersion);
            return new ResponseEntity<>(addOnVersion, addOnVersion == null ? HttpStatus.NO_CONTENT : HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon/recentreleases")
	public Collection<AddOnInfoAndVersions> getRecentReleases(
			@RequestParam(value = "resultsize", defaultValue = "10") int resultSize) throws Exception {
		return index.getRecentReleases(resultSize);
	}

}
