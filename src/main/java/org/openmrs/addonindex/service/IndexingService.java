package org.openmrs.addonindex.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnList;
import org.openmrs.addonindex.domain.AddOnReference;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.openmrs.addonindex.domain.MaterializedAddOnList;
import org.openmrs.addonindex.domain.MaterializedReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexingService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
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
	
	/**
	 * Queries the index to fetch the add-ons referenced in list
	 *
	 * @param list
	 * @return
	 */
	public MaterializedAddOnList materialize(AddOnList list) throws Exception {
		MaterializedAddOnList materialized = new MaterializedAddOnList(list);
		for (AddOnReference reference : list.getAddOns()) {
			AddOnInfoAndVersions info = repository.getByUid(reference.getUid());
			if (info == null) {
				logger.warn("Could not find addon " + reference.getUid() + " in index");
				continue;
			}
			AddOnInfoSummary summary = new AddOnInfoSummary(info);
			
			MaterializedReference materializedReference = new MaterializedReference(reference);
			materializedReference.setDetails(summary);
			
			if (reference.getVersion() != null) {
				Optional<AddOnVersion> addOnVersion = info.getVersion(reference.getVersion());
				if (!addOnVersion.isPresent()) {
					logger.warn(String.format("List %s refers to %s version %s but this version is not indexed",
							list.getUid(),
							reference.getUid(),
							reference.getVersion()));
					continue;
				}
				materializedReference.setAddOnVersion(addOnVersion.get());
			}
			materialized.add(materializedReference);
		}
		
		return materialized;
	}
	
}
