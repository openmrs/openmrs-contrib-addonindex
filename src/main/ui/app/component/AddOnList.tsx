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
import { IAddOn } from "../types";

interface Props {
  addOns: IAddOn[];
  heading?: string;
}

export const AddOnList: React.FC<Props> = ({ addOns, heading }) => {
  if (!addOns) {
    return <></>;
  }

  return (
    <>
      {heading ? <h4>{heading}</h4> : null}
      <Row>
        {addOns.map((addOn) => (
          <Col key={addOn.uid} xs={12}>
            <AddOn addOn={addOn} />
          </Col>
        ))}
      </Row>
    </>
  );
};
