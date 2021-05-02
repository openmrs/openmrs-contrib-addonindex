/*!
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React from "react";
import { render } from "react-dom";
import { Redirect, Route, Switch, useLocation } from "react-router";
import { BrowserRouter as Router } from "react-router-dom";
import {
  About,
  AddOnLists,
  Home,
  IndexingStatus,
  SearchPage,
  Show,
  ShowList,
  TopDownloaded,
} from "./route";

import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";
import dayjs from "dayjs/esm/index";
import relativeTime from "dayjs/esm/plugin/relativeTime";
import localizedFormat from "dayjs/esm/plugin/localizedFormat";
import "./sass/addonindex.scss";
import App from "./App";

// setup fontawesome
library.add(fas, far);

// setup dayjs
dayjs.extend(relativeTime);
dayjs.extend(localizedFormat);

// This is a bit of a hack to try to support legacy URLs
// from the previous version of the AddonIndex which used
// the HashRouter instead of BrowserRouter
const HashRedirect = ({ children }) => {
  const location = useLocation();

  return (
    <>
      {!!!location.search && location.hash?.startsWith("#/") ? (
        <Redirect to={location.hash.replace("#", "")} push />
      ) : (
        children
      )}
    </>
  );
};

render(
  <Router>
    <HashRedirect>
      <App>
        <Switch>
          <Route path="/about" component={About} />
          <Route path="/indexingStatus" component={IndexingStatus} />
          <Route path="/search" component={SearchPage} />
          <Route path="/show/:uid" component={Show} />
          <Route path="/show" render={() => <Redirect to={"/"} />} />
          <Route path="/lists" component={AddOnLists} />
          <Route path="/list/:uid" component={ShowList} />
          <Route path="/list" render={() => <Redirect to={"/lists"} />} />
          <Route path="/topDownloaded" component={TopDownloaded} />
          <Route exact path="/" component={Home} />
          <Redirect to={"/"} push />
        </Switch>
      </App>
    </HashRedirect>
  </Router>,
  document.getElementById("root")
);
