/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.service;

import java.util.Collections;
import java.util.List;

import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {
	
	private final Index repository;
	
	private List<AddOnInfoSummaryAndStats> topDownloaded = Collections.emptyList();
	
	@Autowired
	public AnalysisService(Index repository) {
		this.repository = repository;
	}
	
	@Scheduled(initialDelayString = "${scheduler.analysis.count_top_downloads.initial_delay}", fixedDelayString = "${scheduler.analysis.count_top_downloads.period}")
	public void refreshTopDownloadedList() throws Exception {
		topDownloaded = repository.getTopDownloaded();
	}
	
	public List<AddOnInfoSummaryAndStats> getTopDownloaded() {
		return topDownloaded;
	}
}
