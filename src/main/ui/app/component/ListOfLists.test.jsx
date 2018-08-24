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

import ListOfLists from './ListOfLists';

test("ListOfLists", () => {
    fetch.resetMocks();
    fetch.mockResponseOnce(JSON.stringify(
            [{
                "uid": "highlighted",
                "name": "Featured",
                "description": "Here are some Add Ons you might want to get started with",
                "addOns": [{"uid": "org.openmrs.module.idgen", "version": null}, {
                    "uid": "org.openmrs.module.htmlformentry",
                    "version": null
                }, {"uid": "org.openmrs.module.xforms", "version": null}, {
                    "uid": "org.openmrs.module.reporting",
                    "version": null
                }, {
                    "uid": "org.openmrs.module.admin-ui-module",
                    "version": null
                }, {
                    "uid": "org.openmrs.module.legacy-ui-module",
                    "version": null
                }, {"uid": "org.openmrs.module.registration-core-module", "version": null}]
            }, {
                "uid": "refapp_2_7",
                "name": "RefApp 2.7",
                "description": "Included in Reference Application 2.7",
                "addOns": [{
                    "uid": "org.openmrs.module.admin-ui-module",
                    "version": "1.2.2"
                }, {
                    "uid": "org.openmrs.module.allergy-ui-module",
                    "version": "1.8.0"
                }]
            }]));

    const wrapper = shallow(<ListOfLists/>);
    console.log(wrapper.debug());
    setImmediate(() => {
        wrapper.update();
        expect(wrapper.find('li')).toHaveLength(3);
        // I imagine there's a cleaner way to test these...
        expect(wrapper.find('Link').first().debug().includes('to="/topDownloaded"')).toBe(true);
        expect(wrapper.find('Link').at(1).debug().includes('to="/list/highlighted"')).toBe(true);
        expect(wrapper.find('Link').at(2).debug().includes('to="/list/refapp_2_7"')).toBe(true);
    });
});