package org.openmrs.addonindex.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnType;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryIndex implements Index {
	
	private Map<String, AddOnInfoAndVersions> index = new LinkedHashMap<>();
	
	@Override
	public void index(AddOnInfoAndVersions infoAndVersions) {
		index.put(infoAndVersions.getUid(), infoAndVersions);
	}
	
	@Override
	public Collection<AddOnInfoAndVersions> getAllByType(AddOnType type) {
		if (type == null) {
			return Collections.unmodifiableCollection(index.values());
		}
		return index.values().stream()
				.filter((AddOnInfoAndVersions i) -> i.getType().equals(type))
				.collect(Collectors.toList());
	}
	
	@Override
	public AddOnInfoAndVersions getByUid(String uid) {
		return index.get(uid);
	}
	
}
