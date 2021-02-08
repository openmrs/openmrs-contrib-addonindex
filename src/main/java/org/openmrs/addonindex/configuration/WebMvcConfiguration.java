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

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// It's okay to make cross-origin requests to our API. For example we specifically expect that the OpenMRS OWA for
		// managing modules will do this.
		registry.addMapping("/**");
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// Our ReactJS app is served from the root path, and we want to support using the HTML5 history API for routing
		// and support deep links. But we also want to support REST controllers, e.g. at /api/**. We can't figure out a
		// way to do a wildcard-except-for-api, so instead we map each individual route from the JS app here:
		registry.addViewController("/about").setViewName("forward:/index.html");
		registry.addViewController("/indexingStatus").setViewName("forward:/index.html");
		registry.addViewController("/search").setViewName("forward:/index.html");
		registry.addViewController("/show/**").setViewName("forward:/index.html");
		registry.addViewController("/lists").setViewName("forward:/index.html");
		registry.addViewController("/list/**").setViewName("forward:/index.html");
		registry.addViewController("/topDownloaded").setViewName("forward:/index.html");
	}
}
