package org.openmrs.addonindex.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;
import org.springframework.stereotype.Repository;

/**
 * Quick in-memory implementation of indexing data.
 * Eventually we should replace this with something that supports full-text search, like ElasticSearch. (Though in
 * practice, merely enhancing this class to search on more fields may be sufficient, given the size of the dataset.)
 */
@Repository
public class InMemoryIndex implements Index {
	
	private Map<String, AddOnInfoAndVersions> index = new LinkedHashMap<>();
	
	@Override
	public void index(AddOnInfoAndVersions infoAndVersions) {
		index.put(infoAndVersions.getUid(), infoAndVersions);
	}
	
	@Override
	public Collection<AddOnInfoSummary> search(AddOnType type, String query) {
		final String lowerCaseQuery = query == null ? null : query.toLowerCase();
		return index.values().stream()
				.filter(i -> {
					return (type == null || type.equals(i.getType())) &&
							(lowerCaseQuery == null || i.getName().toLowerCase().contains(lowerCaseQuery));
				})
				.map(AddOnInfoSummary::new)
				.collect(Collectors.toList());
	}
	
	@Override
	public Collection<AddOnInfoAndVersions> getAllByType(AddOnType type) {
		if (type == null) {
			return Collections.unmodifiableCollection(index.values());
		}
		return index.values().stream()
				.filter(i -> i.getType().equals(type))
				.collect(Collectors.toList());
	}
	
	@Override
	public AddOnInfoAndVersions getByUid(String uid) {
		return index.get(uid);
	}
	
}
