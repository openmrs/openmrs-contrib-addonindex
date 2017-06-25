/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.service.artifactory;

import org.openmrs.addonindex.domain.artifactory.VersionList;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class VersionsService {

    private VersionList versionList;

    public void setVersions(Collection<String> coreversion) {
        this.versionList = new VersionList(coreversion);
    }

    public VersionList getVersions() {
        return versionList;
    }
}
