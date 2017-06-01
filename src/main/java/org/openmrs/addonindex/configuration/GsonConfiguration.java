/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.configuration;

import org.openmrs.addonindex.util.Version;
import org.openmrs.addonindex.util.VersionTypeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class GsonConfiguration {
	
	@Bean
	public Gson gson() {
		GsonBuilder builder = new GsonBuilder();
		Converters.registerAll(builder);
		builder.registerTypeAdapter(Version.class, new VersionTypeConverter());
		return builder.create();
	}
	
}
