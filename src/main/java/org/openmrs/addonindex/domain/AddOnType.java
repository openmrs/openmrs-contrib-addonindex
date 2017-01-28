package org.openmrs.addonindex.domain;

public enum AddOnType {
	
	OMOD("omod"),
	OWA("zip");
	
	private final String fileExtension;
	
	AddOnType(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
}
