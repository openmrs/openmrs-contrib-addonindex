package org.openmrs.addonindex.scheduled;

import java.io.IOException;
import java.util.List;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
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
		try {
			List<AddOnToIndex> allToIndex = indexingService.getAllToIndex();
			logger.info("Fetching details for " + allToIndex.size() + " add-ons");
			for (AddOnToIndex toIndex : allToIndex) {
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
		catch (IOException e) {
			throw new RuntimeException(e);
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
