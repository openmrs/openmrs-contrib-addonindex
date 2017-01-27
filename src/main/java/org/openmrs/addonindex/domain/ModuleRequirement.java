package org.openmrs.addonindex.domain;

public class ModuleRequirement {
	
	private String module;
	
	private String version;
	
	public ModuleRequirement() {
	}
	
	public ModuleRequirement(String module, String version) {
		this.module = module;
		this.version = version;
	}
	
	public String getModule() {
		return module;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
}
