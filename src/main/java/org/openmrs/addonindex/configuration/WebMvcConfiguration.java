package org.openmrs.addonindex.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		// Fix handling of /api/v1/addon/org.openmrs.module.appui (otherwise appui is treated as a file extension)
		configurer.setUseRegisteredSuffixPatternMatch(true);
	}
	
}
