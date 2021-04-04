/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { Fragment, useEffect, useMemo, useState } from "react";
import { useQuery } from "react-query";
import { myFetch } from "../utils";
import { Badge, Col, Row } from "react-bootstrap";

// 1 second
const LIVE_REFETCH_INTERVAL = 1000;

// 5 minutes
const LONG_REFETCH_INTERVAL = 5 * 60 * 1000;

export const IndexingStatus = () => {
  const [refetchInterval, setRefetchInterval] = useState(LIVE_REFETCH_INTERVAL);

  const indexingStatusQuery = useQuery(
    ["indexingStatus"],
    () => myFetch("/api/v1/indexingstatus"),
    {
      refetchOnWindowFocus: "always",
      refetchInterval: refetchInterval,
    }
  );

  const status = useMemo(() => indexingStatusQuery.data, [
    indexingStatusQuery.data,
  ]);

  const [okay, error, pending] = useMemo(() => {
    if (!!!status?.statuses) {
      return [null, null, null];
    }

    let [okay, error, pending] = [0, 0, 0];
    Object.values(status.toIndex?.toIndex).forEach((addOn) => {
      const addOnStatus = status.statuses[addOn.uid];
      if (addOnStatus) {
        if (addOnStatus.error) {
          error += 1;
        } else {
          okay += 1;
        }
      } else {
        pending += 1;
      }
    });

    return [okay, error, pending];
  }, [status]);

  useEffect(() => {
    if (pending === 0) {
      if (refetchInterval === LIVE_REFETCH_INTERVAL) {
        setRefetchInterval(LONG_REFETCH_INTERVAL);
      }
    } else {
      if (refetchInterval === LONG_REFETCH_INTERVAL) {
        setRefetchInterval(LIVE_REFETCH_INTERVAL);
      }
    }
  }, [pending, refetchInterval]);

  if (indexingStatusQuery.isLoading) {
    return <>Loading...</>;
  }

  if (indexingStatusQuery.isError) {
    return <></>;
  }

  return (
    <>
      <h3>
        Indexing Status &nbsp;
        {error ? <Badge variant="danger">Error: {error}</Badge> : null}
        &nbsp;
        {pending ? <Badge variant="info">Pending: {pending}</Badge> : null}
        &nbsp;
        {okay ? <Badge variant="success">Okay: {okay}</Badge> : null}
      </h3>
      <hr />
      {status?.toIndex?.toIndex?.map((addOn) => (
        <Fragment key={addOn.uid}>
          <Row xs={2}>
            <Col>
              <strong>{addOn.uid}</strong>
              <br />
              <br />
              {status.statuses[addOn.uid]?.error ? (
                <Badge variant="danger">Error</Badge>
              ) : (
                <Badge variant="success">Okay</Badge>
              )}
            </Col>
            <Col>
              <pre>{JSON.stringify(status?.statuses[addOn.uid], null, 2)}</pre>
            </Col>
          </Row>
          <hr width="100%" />
        </Fragment>
      ))}
    </>
  );
};
