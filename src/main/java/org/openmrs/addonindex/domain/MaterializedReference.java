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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openmrs.addonindex.util.Version;

/**
 * Represents the result of getting a Module and/or Version for a Reference
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MaterializedReference {

	@EqualsAndHashCode.Include
	private String uid;
	
	private Version version;
	
	private AddOnInfoSummary details;
	
	private AddOnVersion addOnVersion;
	
	public MaterializedReference(AddOnReference addOnReference) {
		this.uid = addOnReference.getUid();
		this.version = addOnReference.getVersion();
	}

}
