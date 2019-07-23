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

import RecentReleases from "./RecentReleases";

test('Recent Releases page renders after fetch', () => {
    fetch.resetMocks();
    fetch.mockResponseOnce(JSON.stringify(
    [{
            "uid":"org.openmrs.module.patientflags",
            "modulePackage":"org.openmrs.module.patientflags",
            "moduleId":"patientflags",
            "status":null,
            "type":"OMOD",
            "name":"Patient Flags",
            "description":"Adds the ability to flag patient records",
            "icon":null,
            "tags":null,
            "hostedUrl":"https://bintray.com/openmrs/omod/patientflags",
            "versions":[
                {
                    "version":"3.0.1",
                    "releaseDatetime":"2019-07-16T12:20:51.486Z",
                },
                {
                    "version":"3.0.0",
                    "releaseDatetime":"2019-05-07T12:12:32.93Z"
                }],
            "downloadCountInLast30Days":null,
            "latestVersion":"3.0.1",
            "versionCount":1
    }, {
            "uid": "org.openmrs.module.htmlformentry",
            "modulePackage": "org.openmrs.module.htmlformentry",
            "moduleId": "htmlformentry",
            "status": null,
            "type": "OMOD",
            "name": "HTML Form Entry",
            "description": "FormEntry in-webapp, using HTML forms",
            "icon": "code",
            "tags": [
                "form-entry"
            ],
            "hostedUrl":"https://bintray.com/openmrs/omod/htmlformentry",
            "versions":[
                {
                    "version":"3.9.2",
                    "releaseDatetime":"2019-06-18T13:21:30.253Z",
                },
                {
                    "version":"3.8.0",
                    "releaseDatetime":"2018-11-09T00:34:18.13Z"
                }],
        }]
    ));

    const wrapper = shallow(<RecentReleases/>);
    setImmediate(() => {
        wrapper.update();
        expect(wrapper.find('.recently-released-item')).toHaveLength(2);
        expect(wrapper.find('.recently-released-item').first().find("h3").first().text()).toBe("3.0.1");
    });
})