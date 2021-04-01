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
import { AddOn } from "../component";
import { useQuery } from "react-query";
import { myFetch } from "../utils";
import { Col, Row } from "react-bootstrap";

export const TopDownloaded = () => {
  const downloadQuery = useQuery(["topdownloaded"], () =>
    myFetch("/api/v1/topdownloaded")
  );

  const topDownloaded = useMemo(() => downloadQuery.data, [downloadQuery]);

  if (downloadQuery.isError) {
    return <>{downloadQuery.error.toString()}</>;
  }

  if (downloadQuery.isLoading) {
    // TODO
    return <>Loading...</>;
  }

  return (
    <>
      <h1>Most Downloaded in the last 30 days</h1>
      {downloadQuery.isLoading && (
        <Row>
          <Col className="offset-1">
            <>Loading...</>
          </Col>
        </Row>
      )}
      {topDownloaded?.map((a) => {
        return (
          <Row key={a.summary.uid}>
            <Col xs={1}>
              <h4 style={{ marginTop: "1.4rem" }}>{a.downloadCount}</h4>
            </Col>
            <Col xs={11}>
              <AddOn addOn={a.summary} />
            </Col>
          </Row>
        );
      })}
    </>
  );
};
