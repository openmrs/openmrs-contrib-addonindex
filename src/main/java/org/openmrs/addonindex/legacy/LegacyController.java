/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.legacy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.OpenmrsVersionCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Supports legacy functionality required by the OpenMRS Module API and Legacy UI
 */
@Controller
public class LegacyController {
	
	@Autowired
	private Index index;
	
	@Autowired
	private IndexingService indexingService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@RequestMapping(method = RequestMethod.GET, value = "/modulus/feeds/{moduleId}/update.rdf",
			produces = { "application/rdf+xml", "application/xml", "text/xml; charset=utf-8" })
	@ResponseBody
	public String checkUpdate(@PathVariable("moduleId") String moduleId) throws Exception {
		AddOnInfoAndVersions info = indexingService.getByUid("org.openmrs.module." + moduleId);
		if (info == null) {
			throw new NullPointerException(); // should map to a 404
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<updates configVersion=\"1.1\" moduleId=\"" + moduleId + "\">\n");
		for (AddOnVersion version : info.getVersions()) {
			sb.append("<update>\n");
			sb.append("<currentVersion>" + version.getVersion() + "</currentVersion>\n");
			sb.append("<requireOpenMRSVersion>" + version.getRequireOpenmrsVersion() + "</requireOpenMRSVersion>\n");
			sb.append("<downloadURL>" + version.getDownloadUri() + "</downloadURL>\n");
			sb.append("</update>\n");
		}
		sb.append("</updates>");
		return sb.toString();
	}
	
	/**
	 * According to https://tickets.openmrs.org/browse/MOD-5 there is an even older URL path we need to support (from before
	 * modulus, even)
	 *
	 * @param moduleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/modules/download/{moduleId}/update.rdf")
	@ResponseBody
	public String oldestLegacyGetUpdateRdf(@PathVariable("moduleId") String moduleId) throws Exception {
		return checkUpdate(moduleId);
	}
	
	/**
	 * These parameters are theoretically required but I don't think we need to handle them:
	 * <li>iSortingCols = 1, 2, 3, 4 for name, version, author, description
	 * <li>iSortCol_ =
	 * <li>iSortDir_ = asc/desc
	 *
	 * @param query
	 * @param start
	 * @param length
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/modules/findModules")
	@ResponseBody
	public String findModules(
			@RequestParam(value = "callback", required = false) String callback,
			@RequestParam(value = "sSearch", required = false) String query,
			@RequestParam(value = "iDisplayStart", defaultValue = "0") Integer start,
			@RequestParam(value = "iDisplayLength", defaultValue = "100") Integer length,
			@RequestParam(value = "sEcho", required = false) Integer sEcho,
			@RequestParam(value = "openmrs_version", required = false) String openmrsVersion,
			@RequestParam(value = "excludeModule", required = false) List<String> excludeModuleIds
	) throws Exception {
		Collection<AddOnInfoSummary> results = index.search(AddOnType.OMOD, query, null);
		
		LegacyFindModulesResponse response = new LegacyFindModulesResponse();
		response.setsEcho(sEcho);
		response.setiTotalRecords((int) indexingService.getAllToIndex()
				.getToIndex().stream().filter(i -> i.getType().equals(AddOnType.OMOD)).count());
		for (AddOnInfoSummary result : results) {
			if (shouldExclude(excludeModuleIds, result)) {
				continue;
			}
			AddOnInfoAndVersions full = index.getByUid(result.getUid());
			
			Optional<AddOnVersion> firstMatch = full.getVersions().stream()
					.filter(i ->
							OpenmrsVersionCompareUtil.matchRequiredVersions(openmrsVersion, i.getRequireOpenmrsVersion()))
					.findFirst();
			if (!firstMatch.isPresent()) {
				// no version of this module is suitable for the specified OpenMRS version
				continue;
			}
			
			AddOnVersion matchedVersion = firstMatch.get();
			
			String maintainer = full.getMaintainers() != null && full.getMaintainers().size() > 0 ?
					full.getMaintainers().get(0).getName() : "";
			
			response.addRow(
					matchedVersion.getDownloadUri(),
					result.getName(),
					matchedVersion.getVersion().toString(),
					maintainer,
					result.getDescription());
		}
		
		String json = objectMapper.writeValueAsString(response);
		if (callback != null) {
			return callback + "(" + json + ")";
		} else {
			return json;
		}
	}
	
	private boolean shouldExclude(List<String> excludeModuleIds, AddOnInfoSummary candidate) {
		if (excludeModuleIds == null) {
			return false;
		}
		// excludeModuleIds would just be "appui" but candidate.uid would be something like "org.openmrs.module.appui".
		// this is an imperfect hack to cover most cases
		String lookFor = candidate.getUid().startsWith("org.openmrs.module.") ?
				candidate.getUid().substring("org.openmrs.module.".length()) :
				candidate.getUid();
		return excludeModuleIds.contains(lookFor);
	}
	
}
