package org.openmrs.addonindex.configuration;

import org.openmrs.addonindex.util.Version;
import org.openmrs.addonindex.util.VersionTypeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class GsonConfiguration {
	
	@Bean
	public Gson gson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Version.class, new VersionTypeConverter());
		return builder.create();
	}
	
}
