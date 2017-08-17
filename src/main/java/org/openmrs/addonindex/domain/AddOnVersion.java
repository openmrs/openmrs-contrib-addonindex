/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.addonindex.util.Version;

/**
 * Details about a single version, for use inside {@link AddOnInfoAndVersions}
 */
public class AddOnVersion implements Comparable<AddOnVersion> {
	
	private Version version;
	
	private OffsetDateTime releaseDatetime;
	
	private String downloadUri;
	
	private String renameTo;
	
	private String requireOpenmrsVersion;

    private String modulePackage;

    private String moduleId;

    private List<ModuleRequirement> requireModules;
	
	private List<String> supportedLanguages;
	
	public Version getVersion() {
		return version;
	}
	
	public void setVersion(Version version) {
		this.version = version;
	}
	
	public OffsetDateTime getReleaseDatetime() {
		return releaseDatetime;
	}
	
	public void setReleaseDatetime(OffsetDateTime releaseDatetime) {
		this.releaseDatetime = releaseDatetime;
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

    public String getModulePackage() {
        return modulePackage;
    }

    public void setModulePackage(String modulePackage) {
        this.modulePackage = modulePackage;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

	public List<ModuleRequirement> getRequireModules() {
		return requireModules;
	}
	
	public void setRequireModules(List<ModuleRequirement> requireModules) {
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
			requireModules = new ArrayList<>();
		}
		requireModules.add(new ModuleRequirement(requiredModule, version == null ? "?" : version));
	}
	
	public void addLanguage(String localeCode) {
		if (supportedLanguages == null) {
			supportedLanguages = new ArrayList<>();
		}
		supportedLanguages.add(localeCode);
	}
}
