package org.openmrs.addonindex.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BintrayPackageDetails {
	
	private String owner;
	
	private String repo;
	
	private String packageName; // "package" is a reserved word in Java, so we use this instead, but serialize as package
	
	public BintrayPackageDetails() {
	}
	
	public BintrayPackageDetails(String owner, String repo, String packageName) {
		this.owner = owner;
		this.repo = repo;
		this.packageName = packageName;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getRepo() {
		return repo;
	}
	
	public void setRepo(String repo) {
		this.repo = repo;
	}
	
	@JsonProperty("package")
	public String getPackageName() {
		return packageName;
	}
	
	@JsonProperty("package")
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
