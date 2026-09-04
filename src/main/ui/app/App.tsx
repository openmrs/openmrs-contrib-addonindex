/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, {
  createContext,
  PropsWithChildren,
  useEffect,
  useState,
} from "react";
import { Link, NavLink } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useLocation } from "react-router";
import ReactGA from "react-ga";
import { Col } from "react-bootstrap";
import { ListOfLists, SelectUserVersions } from "./component";

export const CoreVersionContext = createContext(null);
export const HidePlatformPickerContext = createContext<(hide: boolean) => void>(
  () => undefined,
);
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) =>
        error instanceof Response && error.status >= 500 && failureCount < 3,
      refetchOnWindowFocus: false,
    },
  },
});

let GA_ID = "UA-16695719-3";
if (process.env.NODE_ENV === "development") {
  GA_ID = undefined;
}

const Analytics: React.FC<PropsWithChildren<unknown>> = ({ children }) => {
  const location = useLocation();
  useEffect(() => {
    if (process.env.NODE_ENV !== "development") {
      ReactGA.initialize(GA_ID);
    }
  }, []);

  useEffect(() => {
    if (process.env.NODE_ENV !== "development") {
      const fullLocation = location.pathname + location.search;
      ReactGA.set({ page: fullLocation });
      ReactGA.pageview(fullLocation);
    }
  }, [location.pathname, location.search]);

  return <>{children}</>;
};

const App: React.FC<PropsWithChildren<unknown>> = ({ children }) => {
  const [openmrsCoreVersion, setOpenmrsCoreVersion] = useState(null);
  const [hidePlatformPicker, setHidePlatformPicker] = useState(false);
  return (
    <Analytics>
      <QueryClientProvider client={queryClient}>
        <CoreVersionContext.Provider value={openmrsCoreVersion}>
          <HidePlatformPickerContext.Provider value={setHidePlatformPicker}>
            <Col className="container-fluid">
              <header className="clearfix row vertical-align-center">
                <h1 className="col-4">
                  <Link to="/">
                    <img
                      className="logo logo1"
                      src="/images/logo.png"
                      alt="OpenMRS Add-Ons Logo"
                    />
                  </Link>
                </h1>
                <Col sm={5}>
                  <ListOfLists />
                </Col>
                {/* hidden rather than unmounted, so returning to an OMOD page
                    does not refetch the core version list */}
                <Col sm={3} className={hidePlatformPicker ? "d-none" : ""}>
                  <SelectUserVersions updateValue={setOpenmrsCoreVersion} />
                </Col>
              </header>
              <div className="offset-10 text-right">
                <NavLink
                  to={`/about`}
                  className={({ isActive }) => (isActive ? "hidden" : "")}
                >
                  About Add Ons
                </NavLink>
              </div>
              {children}
            </Col>
          </HidePlatformPickerContext.Provider>
        </CoreVersionContext.Provider>
      </QueryClientProvider>
    </Analytics>
  );
};

export default App;
