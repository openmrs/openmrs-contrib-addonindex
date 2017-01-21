package org.openmrs.addonindex.scheduled;

import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fetches the list of Add-Ons that we need to index
 */
@Component
public class FetchAddOnList {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${add_on_list.url}")
	private String url;
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private IndexingService indexingService;
	
	@Scheduled(
			initialDelayString = "${scheduler.fetch_add_on_list.initial_delay}",
			fixedDelayString = "${scheduler.fetch_add_on_list.period}")
	public void fetchAddOnList() throws Exception {
		logger.info("Fetching list of add-ons to index");
		
		String json = restTemplateBuilder.build().getForObject(url, String.class);
		AllAddOnsToIndex toIndex;
		try {
			toIndex = mapper.readValue(json, AllAddOnsToIndex.class);
		}
		catch (Exception ex) {
			throw new RuntimeException("File downloaded from " + url + " could not be parsed", ex);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("We have " + toIndex.getToIndex().size() + " add-ons to index");
		}
		if (toIndex.size() > 0) {
			indexingService.setAllToIndex(toIndex);
		} else {
			logger.warn("File downloaded from " + url + " does not list any add-ons to index. Keeping our current list");
		}
	}
	
}
