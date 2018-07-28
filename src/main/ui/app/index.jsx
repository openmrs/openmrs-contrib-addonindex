/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React from "react";
import {render} from "react-dom";
import {Router, browserHistory} from "react-router";
import routes from "./routes";
import ReactGA from 'react-ga';

import "./sass/addonindex.scss";

if (GA_ID) {
    ReactGA.initialize(GA_ID);
}

function trackView() {
    if (GA_ID) {
        ReactGA.pageview(window.location.pathname + window.location.search);
    }
}

render(
        <Router onUpdate={trackView} history={browserHistory} routes={routes}/>,
        document.getElementById('root')
);