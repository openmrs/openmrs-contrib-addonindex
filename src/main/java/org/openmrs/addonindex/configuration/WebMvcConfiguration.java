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
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// I don't suppose there's too much of a risk to enabling CORS for everywhere, but here we restrict things to
		// the known API paths. Of course, Spring's @CrossOrigin annotation could be used to enable CORS for URLs that
		// don't match these patterns

		registry
				.addMapping("/api/**")
				.allowedMethods(RequestMethod.GET.name())
				.maxAge(1800L);

		registry
				.addMapping("/{path:^modul(?:us|es)$}/**")
				.allowedMethods(RequestMethod.GET.name())
				.maxAge(1800L);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// This is a little verbose, but it allows us to serve static content correctly and not need to manually map
		// every app URL in two places. Basically, everything is treated as either:
		//  1. A request for a known API (whether legacy or the REST API)
		//  2. A request for a static resource from a known location
		//  3. A request that should be resolved to the default SPA page.

		// known static resource paths
		registry
				.addResourceHandler("/*.js")
				.setCachePeriod(Integer.MAX_VALUE)
				.addResourceLocations("classpath:/static/");

		registry
				.addResourceHandler("/*.css")
				.setCachePeriod(Integer.MAX_VALUE)
				.addResourceLocations("classpath:/static/");

		registry
				.addResourceHandler("/*.map")
				.setCachePeriod(Integer.MAX_VALUE)
				.addResourceLocations("classpath:/static/");

		registry
				.addResourceHandler("/images/**")
				.setCachePeriod(0)
				.addResourceLocations("classpath:/static/images/");

		registry
				.addResourceHandler("/favicon.ico")
				.setCachePeriod(0)
				.addResourceLocations("classpath:/static/");

		// the trick here is that for everything else, we either resolve it to a path
		// Spring knows about or we just serve the index.html page
		registry
				.addResourceHandler("/", "/**")
				.setCachePeriod(0)
				.addResourceLocations("classpath:/static/index.html")
				.resourceChain(true)
				.addResolver(new PathResourceResolver() {

					@Override
					protected Resource getResource(String resourcePath, Resource location) {
						return location.exists() && location.isReadable() ? location : null;
					}
				});
	}
}
