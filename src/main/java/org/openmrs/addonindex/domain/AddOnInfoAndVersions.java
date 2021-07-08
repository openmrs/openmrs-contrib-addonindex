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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.addonindex.util.OpenmrsVersionCompareUtil;
import org.openmrs.addonindex.util.Version;

/**
 * Details about an OpenMRS add-on and its available versions
 */
@Data
@NoArgsConstructor
public class AddOnInfoAndVersions {
	
	public final static String ES_INDEX = "add_on_info_and_versions";

	@EqualsAndHashCode.Include
	private String uid;

	@EqualsAndHashCode.Include
	private String modulePackage;

	@EqualsAndHashCode.Include
	private String moduleId;

	private AddOnStatus status;
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
	private String icon;
	
	private List<String> tags;
	
	private List<Maintainer> maintainers;
	
	private List<Link> links;
	
	private String hostedUrl;
	
	private List<AddOnVersion> versions = new ArrayList<>();

	private Integer downloadCountInLast30Days;
	
	public static AddOnInfoAndVersions from(AddOnToIndex toIndex) {
		AddOnInfoAndVersions ret = new AddOnInfoAndVersions();
		ret.setUid(toIndex.getUid());
		ret.setStatus(toIndex.getStatus());
		ret.setName(toIndex.getName());
		ret.setDescription(toIndex.getDescription());
		ret.setIcon(toIndex.getIcon());
		ret.setTags(toIndex.getTags());
		ret.setType(toIndex.getType());
		ret.setMaintainers(toIndex.getMaintainers());
		ret.setLinks(toIndex.getLinks());
		return ret;
	}
	
	public void addVersion(AddOnVersion version) {
		versions.add(version);
		versions.sort(Comparator.reverseOrder());
	}

	public int getVersionCount() {
		return versions == null ? 0 : versions.size();
	}
	
	public Version getLatestVersion() {
		return versions == null || versions.size() == 0 ? null : Collections.max(versions).getVersion();
	}

	public Optional<AddOnVersion> getVersion(Version version) {
		if (versions == null) {
			return Optional.empty();
		}

		return versions.stream().filter(v -> v.getVersion().equals(version)).findFirst();
	}
	
	public void addTag(String tag) {
		if (!Pattern.matches("[^\\s]*", tag)) {
			throw new IllegalArgumentException("Tag cannot contain whitespace:" + tag);
		}

		if (tags == null) {
			tags = new ArrayList<>();
		}

		tags.add(tag);
	}

    public AddOnVersion getLatestSupportedVersion(String userCoreVersion) {
        if (userCoreVersion != null) {
            for (AddOnVersion addOnVersion : getVersions()) {
                if (OpenmrsVersionCompareUtil.matchRequiredVersions(userCoreVersion, addOnVersion.getRequireOpenmrsVersion())) {
                    return addOnVersion;
                }
            }
            return null;
        }
        return getVersion(getLatestVersion()).isPresent() ? getVersion(getLatestVersion()).get() : null;
    }
	
	public void setDetailsBasedOnLatestVersion() {
		Optional<AddOnVersion> version = getVersion(getLatestVersion());
		if (version.isPresent()) {
			setModuleId(version.get().getModuleId());
			setModulePackage(version.get().getModulePackage());
		}
	}
}
