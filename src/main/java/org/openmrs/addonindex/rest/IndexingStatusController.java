package org.openmrs.addonindex.rest;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.addonindex.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexingStatusController {
	
	private IndexingService service;
	
	@Autowired
	public IndexingStatusController(IndexingService service) {
		this.service = service;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/indexingstatus")
	public Map<String, Object> checkStatus() throws Exception {
		Map<String, Object> ret = new LinkedHashMap<>();
		ret.put("toIndex", service.getAllToIndex());
		ret.put("statuses", service.getIndexingStatus().getStatuses());
		return ret;
	}
	
}
