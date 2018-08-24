package org.openmrs.addonindex.rest;

import java.util.List;

import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.openmrs.addonindex.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopDownloadedController {
	
	private AnalysisService analysisService;
	
	@Autowired
	public TopDownloadedController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/topdownloaded")
	public List<AddOnInfoSummaryAndStats> getTopDownloaded() {
		return analysisService.getTopDownloaded();
	}
	
}
