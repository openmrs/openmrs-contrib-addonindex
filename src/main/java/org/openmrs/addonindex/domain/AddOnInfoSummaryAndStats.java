package org.openmrs.addonindex.domain;

public class AddOnInfoSummaryAndStats {
	
	private AddOnInfoSummary summary;
	
	private Integer downloadCount;
	
	public AddOnInfoSummaryAndStats() {
	}
	
	public AddOnInfoSummaryAndStats(AddOnInfoAndVersions addOn) {
		this.summary = new AddOnInfoSummary(addOn);
		downloadCount = addOn.getDownloadCountInLast30Days();
	}
	
	public AddOnInfoSummary getSummary() {
		return summary;
	}
	
	public void setSummary(AddOnInfoSummary summary) {
		this.summary = summary;
	}
	
	public Integer getDownloadCount() {
		return downloadCount;
	}
	
	public void setDownloadCount(Integer downloadCount) {
		this.downloadCount = downloadCount;
	}
}
