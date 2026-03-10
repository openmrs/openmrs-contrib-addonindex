/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex;

import java.io.IOException;
import java.nio.charset.Charset;

import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtil {
	
	public static String getFileAsString(String resourcePath) throws IOException {
		return StreamUtils.copyToString(TestUtil.class.getClassLoader().getResourceAsStream(resourcePath),
		    Charset.defaultCharset());
	}
	
	public static void loadLocalAddOnsToIndex(ObjectMapper objectMapper, IndexingService indexingService)
	        throws IOException {
		String json = getFileAsString("add-ons-to-index.json");
		AllAddOnsToIndex allAddOns = objectMapper.readValue(json, AllAddOnsToIndex.class);
		indexingService.setAllToIndex(allAddOns);
	}
	
}
