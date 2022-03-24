package org.openmrs.addonindex.domain.nexus3;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Nexus3SearchResponse {
	
	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Nexus3SearchItemDetails {
		
		private String downloadUrl;
		
		private String path;
	}
	
	private List<Nexus3SearchItemDetails> items;
}
