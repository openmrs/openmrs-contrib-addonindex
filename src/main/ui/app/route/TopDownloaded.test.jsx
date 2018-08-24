/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React from 'react';
import {shallow} from 'enzyme';

import TopDownloaded from "./TopDownloaded";

test('Top Downloaded page renders after fetch', () => {
    fetch.resetMocks();
    fetch.mockResponseOnce(JSON.stringify(
            [{
                "summary": {
                    "uid": "org.openmrs.owa.cohortbuilder",
                    "status": null,
                    "type": "OWA",
                    "name": "Cohort Builder OWA",
                    "description": "The cohort-builder open web app, is an OpenMRS tool used to generate reports as per the example on an ad hoc basis. This means that it builds a cohort of patients, based on the similarity of their characteristics.",
                    "icon": null,
                    "tags": null,
                    "versionCount": 2,
                    "latestVersion": "1.0.0"
                }, "downloadCount": 1159
            }, {
                "summary": {
                    "uid": "org.openmrs.module.addresshierarchy",
                    "status": null,
                    "type": "OMOD",
                    "name": "Address Hierarchy",
                    "description": "Allows for the entry of structured addresses.",
                    "icon": null,
                    "tags": null,
                    "versionCount": 29,
                    "latestVersion": "2.11.0"
                }, "downloadCount": 448
            }]
    ));

    const wrapper = shallow(<TopDownloaded/>);
    setImmediate(() => {
        wrapper.update();
        expect(wrapper.find('.most-downloaded-item')).toHaveLength(2);
        expect(wrapper.find('.most-downloaded-item').first().find("h3").text()).toBe("1159");
    });
})