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
import java.util.NoSuchElementException;

import org.openmrs.addonindex.domain.AddOnList;
import org.openmrs.addonindex.domain.MaterializedAddOnList;
import org.openmrs.addonindex.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ListController {
	
	private IndexingService indexingService;
	
	@Autowired
	public ListController(IndexingService indexingService) {
		this.indexingService = indexingService;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/list")
	public Collection<AddOnList> getAllLists() {
		return indexingService.getAllToIndex().getLists();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/list/{uid}")
	public MaterializedAddOnList getOne(@PathVariable String uid) throws Exception {
		AddOnList list;
		if (uid.equals("DEFAULT")) {
			if (indexingService.getAllToIndex().getLists().isEmpty()) {
				throw new NoSuchElementException("Lists not available during server startup");
			}
			list = indexingService.getAllToIndex().getLists().get(0);
		} else {
			list = indexingService.getAllToIndex().getListByUid(uid).get();
		}
		return indexingService.materialize(list);
	}
	
}
