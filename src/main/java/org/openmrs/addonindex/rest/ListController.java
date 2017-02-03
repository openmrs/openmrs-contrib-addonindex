package org.openmrs.addonindex.rest;

import java.util.Collection;

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
		AddOnList list = indexingService.getAllToIndex().getListByUid(uid).get();
		return indexingService.materialize(list);
	}
	
}
