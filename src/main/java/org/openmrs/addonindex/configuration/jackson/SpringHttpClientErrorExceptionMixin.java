package org.openmrs.addonindex.configuration.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Normally when Jackson tries to serialize this it ends up with "Direct self-reference leading to cycle" through the
 * 'mostSpecificCause' property
 */
public abstract class SpringHttpClientErrorExceptionMixin {
	
	@JsonIgnore
	abstract Throwable getMostSpecificCause();
	
}
