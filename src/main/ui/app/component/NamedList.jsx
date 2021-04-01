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
import { Col, Row } from "react-bootstrap";
import { AddOn } from "./AddOn";

export const NamedList = ({ list }) => {
  return (
    <>
      <h1>{list.name}</h1>
      <h3>{list.description}</h3>
      <Row>
        <Col xs={12}>
          {list.addOns.map((addOn) => (
            <AddOn
              key={addOn.uid}
              addOn={addOn.details}
              version={addOn.version}
            />
          ))}
        </Col>
      </Row>
    </>
  );
};
