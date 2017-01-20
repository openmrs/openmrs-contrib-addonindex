package org.openmrs.addonindex.rest;

import java.util.Collection;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.service.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddOnController {
	
	@Autowired
	private Index index;
	
	@RequestMapping("/api/v1/addon")
	public Collection<AddOnInfoAndVersions> getAll(@RequestParam(value = "type", required = false) AddOnType type) {
		return index.getAllByType(type);
	}
	
	@RequestMapping("/api/v1/addon/{uid}")
	public AddOnInfoAndVersions getOne(@PathVariable String uid) {
		return index.getByUid(uid);
	}
	
}
