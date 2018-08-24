package org.openmrs.addonindex.service;

import java.util.Collections;
import java.util.List;

import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {
	
	private Index repository;
	
	private List<AddOnInfoSummaryAndStats> topDownloaded = Collections.emptyList();
	
	@Autowired
	public AnalysisService(Index repository) {
		this.repository = repository;
	}
	
	@Scheduled(
			initialDelayString = "${scheduler.analysis.count_top_downloads.initial_delay}",
			fixedDelayString = "${scheduler.analysis.count_top_downloads.period}")
	public void refreshTopDownloadedList() throws Exception {
		topDownloaded = repository.getTopDownloaded();
	}
	
	public List<AddOnInfoSummaryAndStats> getTopDownloaded() {
		return topDownloaded;
	}
}
