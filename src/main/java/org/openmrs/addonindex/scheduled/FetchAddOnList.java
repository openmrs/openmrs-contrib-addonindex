package org.openmrs.addonindex.scheduled;

import java.io.InputStream;
import java.util.List;

import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

@Component
public class FetchAddOnList {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IndexingService indexingService;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	private CollectionType listOfItemsType = objectMapper.getTypeFactory().constructCollectionType(List.class,
			AddOnToIndex.class);
	
	@Scheduled(
			initialDelayString = "${scheduler.fetch_add_on_list.initial_delay}",
			fixedDelayString = "${scheduler.fetch_add_on_list.period}")
	public void fetchAddOnList() throws Exception {
		logger.info("Fetching list of add-ons to index");
		
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("toIndex.json");
		List<AddOnToIndex> allToIndex = objectMapper.readValue(resourceStream, listOfItemsType);
		
		if (logger.isDebugEnabled()) {
			logger.debug("We have " + allToIndex.size() + " add-ons to index");
		}
		indexingService.setAllToIndex(allToIndex);
	}
	
}
