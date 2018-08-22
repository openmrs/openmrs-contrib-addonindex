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

import ExternalLink from './ExternalLink';

test('Link shows title when given', () => {
    const link = {
        href: "www.google.com",
        title: "Google"
    };
    const wrapper = shallow(<ExternalLink link={link}/>);
    expect(wrapper.matchesElement(<a target="_blank" href="www.google.com">Google</a>)).toBe(true);
});

test('Link shows href when no title given', () => {
    const link = {
        href: "www.google.com",
    };
    const wrapper = shallow(<ExternalLink link={link}/>);
    expect(wrapper.matchesElement(<a target="_blank" href="www.google.com">www.google.com</a>)).toBe(true);
});