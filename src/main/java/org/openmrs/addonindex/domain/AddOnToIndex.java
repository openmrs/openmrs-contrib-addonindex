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

import java.util.List;

import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.backend.BintrayPackageDetails;
import org.openmrs.addonindex.backend.MavenRepoDetails;
import org.openmrs.addonindex.backend.ModulusModuleDetails;

/**
 * An OpenMRS add-on that should be indexed.
 * Authors add these to our registry, and the application takes care of indexing them.
 */
public class AddOnToIndex {
	
	private String uid;

	private AddOnStatus status;
	
	private AddOnType type;
	
	private String name;
	
	private String description;
	
	private String icon;
	
	private List<String> tags;
	
	private List<Maintainer> maintainers;
	
	private List<Link> links;
	
	private Class<? extends BackendHandler> backend;
	
	private MavenRepoDetails mavenRepoDetails;
	
	private BintrayPackageDetails bintrayPackageDetails;
	
	private ModulusModuleDetails modulusDetails;
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}

	public AddOnStatus getStatus() {
		return status;
	}

	public void setStatus(AddOnStatus status) {
		this.status = status;
	}
	
	public AddOnType getType() {
		return type;
	}
	
	public void setType(AddOnType type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public List<Maintainer> getMaintainers() {
		return maintainers;
	}
	
	public void setMaintainers(List<Maintainer> maintainers) {
		this.maintainers = maintainers;
	}
	
	public List<Link> getLinks() {
		return links;
	}
	
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	
	public Class<? extends BackendHandler> getBackend() {
		return backend;
	}
	
	public void setBackend(Class<? extends BackendHandler> backend) {
		this.backend = backend;
	}
	
	public MavenRepoDetails getMavenRepoDetails() {
		return mavenRepoDetails;
	}
	
	public void setMavenRepoDetails(MavenRepoDetails mavenRepoDetails) {
		this.mavenRepoDetails = mavenRepoDetails;
	}
	
	public BintrayPackageDetails getBintrayPackageDetails() {
		return bintrayPackageDetails;
	}
	
	public void setBintrayPackageDetails(BintrayPackageDetails bintrayPackageDetails) {
		this.bintrayPackageDetails = bintrayPackageDetails;
	}
	
	public ModulusModuleDetails getModulusDetails() {
		return modulusDetails;
	}
	
	public void setModulusDetails(ModulusModuleDetails modulusDetails) {
		this.modulusDetails = modulusDetails;
	}
}
