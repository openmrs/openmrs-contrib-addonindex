/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.rest.artifactory;

import org.openmrs.addonindex.service.artifactory.VersionsService;
import org.openmrs.addonindex.util.Version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.SortedSet;

@RestController
public class CoreVersionsController {

    private VersionsService versionsService;

    @Autowired
    public CoreVersionsController(VersionsService versionsService) {
        this.versionsService = versionsService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/v1/coreversions")
    public SortedSet<Version> coreversions() throws Exception {
        return versionsService.getVersions().getVersions();
    }
}
