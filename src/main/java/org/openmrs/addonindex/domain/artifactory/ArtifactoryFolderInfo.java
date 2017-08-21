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

import java.util.List;

/**
 * Represents the response from https://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API#ArtifactoryRESTAPI-FolderInfo
 * We use this to determine the list of OpenMRS core versions
 */

public class ArtifactoryFolderInfo {

    private List<ArtifactoryFolderInfoChild> versions;

    public List<ArtifactoryFolderInfoChild> getChildren() {
        return versions;
    }

    public void setChildren(List<ArtifactoryFolderInfoChild> input) {
        this.versions = input;
    }

    public int size() {
        return versions == null ? 0 : versions.size();
    }
}
