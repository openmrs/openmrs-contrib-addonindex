/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { useMemo } from "react";
import { Nav } from "react-bootstrap";
import { useQuery } from "react-query";
import { myFetch } from "../utils";
import { Link } from "react-router-dom";
import { useLocation, useRouteMatch } from "react-router";

const LISTS_TO_SHOW = 3;

export const ListOfLists = () => {
  const location = useLocation();
  const match = useRouteMatch(location.pathname);

  const listQuery = useQuery(["lists"], () => myFetch("/api/v1/list"));

  const lists = useMemo(() => {
    if (!listQuery.data || !Array.isArray(listQuery.data)) {
      return [];
    }

    const lists = listQuery.data;
    lists.unshift({
      uid: "top",
      name: "Top",
      route: "/topDownloaded",
    });

    return lists;
  }, [listQuery.data]);
  const toShow = useMemo(() => lists.slice(0, LISTS_TO_SHOW), [lists]);
  const anyMore = useMemo(() => lists.length > LISTS_TO_SHOW, [lists]);

  if (listQuery.isLoading) {
    return <></>;
  }

  if (listQuery.isError) {
    return <></>;
  }

  if (lists.length === 0) {
    return <></>;
  }

  return (
    <Nav variant="pills">
      {toShow.map((list) => {
        const listRoute = list.route ? list.route : `/list/${list.uid}`;
        return (
          <Nav.Item key={listRoute}>
            <Link
              className={
                "nav-link" + (match.path === listRoute ? " active" : "")
              }
              to={listRoute}
            >
              <strong>{list.name}</strong>
            </Link>
          </Nav.Item>
        );
      })}
      {anyMore ? (
        <Nav.Item>
          <Link className="nav-link" to="/lists">
            More Collections...
          </Link>
        </Nav.Item>
      ) : null}
    </Nav>
  );
};
