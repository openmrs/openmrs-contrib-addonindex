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
import { Link } from "react-router-dom";
import { ListGroup, ListGroupItem, Row } from "react-bootstrap";
import { useQuery } from "react-query";
import { myFetch } from "../utils";

export const AddOnLists = () => {
  const listQuery = useQuery(["collections"], () => myFetch("/api/v1/list"));

  const lists = useMemo(() => (!!!listQuery.data ? null : listQuery.data), [
    listQuery,
  ]);

  if (listQuery.isError) {
    return <></>;
  }

  if (listQuery.isLoading) {
    return <>Loading...</>;
  }

  return (
    <>
      <h3>Collections of Add Ons</h3>
      <ListGroup>
        {lists?.map((list) => (
          <ListGroupItem key={list.uid} style={{ paddingTop: "0.5rem" }}>
            <Link to={`/list/${list.uid}`}>
              <Row>
                <h3 className="col-2 col-sm-3">{list.name}</h3>
                <h5
                  className="col-10 col-sm-9"
                  style={{ paddingTop: "0.3rem", color: "#777" }}
                >
                  {list.description ? list.description : ""}
                </h5>
              </Row>
            </Link>
          </ListGroupItem>
        ))}
      </ListGroup>
    </>
  );
};
