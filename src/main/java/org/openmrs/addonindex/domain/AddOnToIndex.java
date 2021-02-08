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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.domain.backend.BintrayPackageDetails;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.openmrs.addonindex.domain.backend.ModulusModuleDetails;

/**
 * An OpenMRS add-on that should be indexed.
 * Authors add these to our registry, and the application takes care of indexing them.
 */

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class AddOnToIndex {

	@EqualsAndHashCode.Include
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

}
