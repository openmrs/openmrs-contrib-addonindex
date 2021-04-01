package org.openmrs.addonindex.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddOnInfoSummaryAndStats {
	
	private AddOnInfoSummary summary;
	
	private Integer downloadCount;
	
	public AddOnInfoSummaryAndStats(AddOnInfoAndVersions addOn) {
		this.summary = new AddOnInfoSummary(addOn);
		downloadCount = addOn.getDownloadCountInLast30Days();
	}

}
