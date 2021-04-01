package org.openmrs.addonindex.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Link {
	
	private String rel;
	
	private String href;
	
	private String title;
	
}
