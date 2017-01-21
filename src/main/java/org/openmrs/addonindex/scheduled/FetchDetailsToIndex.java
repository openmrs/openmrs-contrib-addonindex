package org.openmrs.addonindex.scheduled;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * For each of the Add-Ons we're supposed to index, uses its backend handler to fetch details and available versions
 */
@Component
public class FetchDetailsToIndex {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IndexingService indexingService;
	
	@Scheduled(
			initialDelayString = "${scheduler.fetch_details_to_index.initial_delay}",
			fixedDelayString = "${scheduler.fetch_details_to_index.period}")
	public void run() {
		AllAddOnsToIndex allToIndex = indexingService.getAllToIndex();
		logger.info("Fetching details for " + allToIndex.size() + " add-ons");
		for (AddOnToIndex toIndex : allToIndex.getToIndex()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Running scheduled index for " + toIndex.getUid());
			}
			try {
				getDetailsAndIndex(toIndex);
			}
			catch (Exception e) {
				logger.error("Error getting details for " + toIndex.getUid(), e);
			}
		}
	}
	
	private void getDetailsAndIndex(AddOnToIndex toIndex) throws Exception {
		BackendHandler handler = indexingService.getHandlerFor(toIndex);
		AddOnInfoAndVersions infoAndVersions = handler.getInfoAndVersionsFor(toIndex);
		if (logger.isDebugEnabled()) {
			logger.debug(toIndex.getUid() + " has " + infoAndVersions.getVersions().size() + " versions");
		}
		indexingService.index(infoAndVersions);
	}
	
}
