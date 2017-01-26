package org.openmrs.addonindex.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.addonindex.util.Version;

/**
 * Details about a single version, for use inside {@link AddOnInfoAndVersions}
 */
public class AddOnVersion implements Comparable<AddOnVersion> {
	
	private Version version;
	
	private String downloadUri;
	
	private String renameTo;
	
	private String requireOpenmrsVersion;
	
	private Map<String, String> requireModules;
	
	private List<String> supportedLanguages;
	
	public Version getVersion() {
		return version;
	}
	
	public void setVersion(Version version) {
		this.version = version;
	}
	
	public String getDownloadUri() {
		return downloadUri;
	}
	
	public void setDownloadUri(String downloadUri) {
		this.downloadUri = downloadUri;
	}
	
	public String getRenameTo() {
		return renameTo;
	}
	
	public void setRenameTo(String renameTo) {
		this.renameTo = renameTo;
	}
	
	public String getRequireOpenmrsVersion() {
		return requireOpenmrsVersion;
	}
	
	public void setRequireOpenmrsVersion(String requireOpenmrsVersion) {
		// hack to clean out a particular illegal value
		if ("${openMRSVersion}".equals(requireOpenmrsVersion)) {
			return;
		}
		this.requireOpenmrsVersion = requireOpenmrsVersion;
	}
	
	public Map<String, String> getRequireModules() {
		return requireModules;
	}
	
	public void setRequireModules(Map<String, String> requireModules) {
		this.requireModules = requireModules;
	}
	
	public List<String> getSupportedLanguages() {
		return supportedLanguages;
	}
	
	public void setSupportedLanguages(List<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}
	
	@Override
	public int compareTo(AddOnVersion other) {
		if (other == null) {
			return -1;
		}
		return this.getVersion().compareTo(other.getVersion());
	}
	
	public void addRequiredModule(String requiredModule, String version) {
		if (requireModules == null) {
			requireModules = new LinkedHashMap<>();
		}
		requireModules.put(requiredModule, version == null ? "?" : version);
	}
	
	public void addLanguage(String localeCode) {
		if (supportedLanguages == null) {
			supportedLanguages = new ArrayList<>();
		}
		supportedLanguages.add(localeCode);
	}
}
