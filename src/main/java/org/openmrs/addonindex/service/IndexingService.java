package org.openmrs.addonindex.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexingService {
	
	@Autowired
	private Index repository;
	
	private List<AddOnToIndex> allToIndex = new ArrayList<>();
	
	private Map<Class<? extends BackendHandler>, BackendHandler> handlers;
	
	@Autowired
	public void setHandlers(List<BackendHandler> allHandlers) {
		handlers = new HashMap<>();
		for (BackendHandler handler : allHandlers) {
			handlers.put(handler.getClass(), handler);
		}
	}
	
	public void setAllToIndex(List<AddOnToIndex> allToIndex) {
		this.allToIndex = allToIndex;
	}
	
	public List<AddOnToIndex> getAllToIndex() throws IOException {
		return Collections.unmodifiableList(allToIndex);
	}
	
	public BackendHandler getHandlerFor(AddOnToIndex toIndex) {
		return handlers.get(toIndex.getBackend());
	}
	
	public void index(AddOnInfoAndVersions infoAndVersions) {
		repository.index(infoAndVersions);
	}
}
