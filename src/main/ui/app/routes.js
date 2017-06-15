/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import App from "./route/App";
import Home from "./route/Home";
import About from "./route/About";
import Show from "./route/Show";
import SearchPage from "./route/SearchPage";
import AddOnLists from "./route/AddOnLists";
import ShowList from "./route/ShowList";
import IndexingStatus from "./route/IndexingStatus";

export default {
    path: '/',
    component: App,
    indexRoute: {component: Home},
    childRoutes: [
        {path: 'about', component: About},
        {path: 'indexingStatus', component: IndexingStatus},
        {path: 'search', component: SearchPage},
        {path: 'show/:uid', component: Show},
        {path: 'lists', component: AddOnLists},
        {path: 'list/:uid', component: ShowList},
    ]
}
