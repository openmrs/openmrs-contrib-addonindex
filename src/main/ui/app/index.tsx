/*!
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { PropsWithChildren } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router";
import { BrowserRouter as Router } from "react-router-dom";
import { ErrorBoundary } from "react-error-boundary";
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
import { ErrorFallback } from "./route/ErrorFallback";

import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";
import dayjs from "dayjs/esm/index";
import relativeTime from "dayjs/esm/plugin/relativeTime";
import localizedFormat from "dayjs/esm/plugin/localizedFormat";
import "./sass/addonindex.scss";
import App from "./App";
import { createRoot } from "react-dom/client";

// setup fontawesome
library.add(fas, far);

// setup dayjs
dayjs.extend(relativeTime);
dayjs.extend(localizedFormat);

// This is a bit of a hack to try to support legacy URLs
// from the previous version of the AddonIndex which used
// the HashRouter instead of BrowserRouter
const HashRedirect: React.FC<PropsWithChildren<unknown>> = ({ children }) => {
  const location = useLocation();

  return (
    <>
      {!location.search && location.hash?.startsWith("#/") ? (
        <Navigate to={location.hash.replace("#", "")} />
      ) : (
        children
      )}
    </>
  );
};

createRoot(document.getElementById("root")).render(
  <Router>
    <HashRedirect>
      <App>
        <ErrorBoundary FallbackComponent={ErrorFallback}>
          <Routes>
            <Route path="/about" element={<About />} />
            <Route path="/indexingStatus" element={<IndexingStatus />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/show/:uid" element={<Show />} />
            <Route path="/show" element={<Navigate to={"/"} />} />
            <Route path="/lists" element={<AddOnLists />} />
            <Route path="/list/:uid" element={<ShowList />} />
            <Route path="/list" element={<Navigate to={"/lists"} />} />
            <Route path="/topDownloaded" element={<TopDownloaded />} />
            <Route path="/" element={<Home />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </ErrorBoundary>
      </App>
    </HashRedirect>
  </Router>
);
