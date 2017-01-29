package org.openmrs.addonindex.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexingService {
	
	private Index repository;
	
	private AllAddOnsToIndex allToIndex = new AllAddOnsToIndex();
	
	private IndexingStatus indexingStatus = new IndexingStatus();
	
	private Map<Class<? extends BackendHandler>, BackendHandler> handlers;
	
	@Autowired
	public IndexingService(Index repository) {
		this.repository = repository;
	}
	
	@Autowired
	public void setHandlers(List<BackendHandler> allHandlers) {
		handlers = new HashMap<>();
		for (BackendHandler handler : allHandlers) {
			handlers.put(handler.getClass(), handler);
		}
	}
	
	public AllAddOnsToIndex getAllToIndex() {
		return allToIndex;
	}
	
	public void setAllToIndex(AllAddOnsToIndex allToIndex) {
		this.allToIndex = allToIndex;
	}
	
	public BackendHandler getHandlerFor(AddOnToIndex toIndex) {
		return handlers.get(toIndex.getBackend());
	}
	
	public void index(AddOnInfoAndVersions infoAndVersions) throws Exception {
		repository.index(infoAndVersions);
	}
	
	public AddOnInfoAndVersions getByUid(String uid) throws Exception {
		return repository.getByUid(uid);
	}
	
	public IndexingStatus getIndexingStatus() {
		return indexingStatus;
	}
}
