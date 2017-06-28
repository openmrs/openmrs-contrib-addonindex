/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.domain.artifactory;

import org.openmrs.addonindex.util.Version;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

public class VersionList {

    private SortedSet<Version> versions;

    public VersionList(Collection<String> coreversions) {
        this.versions = new TreeSet<>();
        for (String candidateVersion : coreversions) {
            this.versions.add(new Version(candidateVersion));
        }
    }

    public SortedSet<Version> getVersionSortedSet() {
        return versions;
    }


}
