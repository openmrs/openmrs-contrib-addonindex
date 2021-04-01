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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.addonindex.util.Version;

/**
 * Details about a single version, for use inside {@link AddOnInfoAndVersions}
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class AddOnVersion implements Comparable<AddOnVersion> {

	@EqualsAndHashCode.Include
	private Version version;
	
	private OffsetDateTime releaseDatetime;
	
	private String downloadUri;
	
	private String renameTo;
	
	private String requireOpenmrsVersion;

	@EqualsAndHashCode.Include
    private String modulePackage;

	@EqualsAndHashCode.Include
    private String moduleId;

    private List<ModuleRequirement> requireModules;
	
	private List<String> supportedLanguages;
	
	public void setRequireOpenmrsVersion(String requireOpenmrsVersion) {
		// hack to clean out a particular illegal value
		if ("${openMRSVersion}".equals(requireOpenmrsVersion)) {
			return;
		}
		this.requireOpenmrsVersion = requireOpenmrsVersion;
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
