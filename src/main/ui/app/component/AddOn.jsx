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
import { Badge, Media } from "react-bootstrap";
import { LegacyFaIcon } from "./LegacyFaIcon";

const addOnItemStyle = { marginRight: "1rem", width: "3.7rem" };

const Title = ({ addOn }) => {
  const status = addOn?.status?.toUpperCase();
  const hasBadge = status === "DEPRECATED" || status === "INACTIVE";
  const variant = hasBadge
    ? status === "DEPRECATED"
      ? "danger"
      : "warning"
    : null;

  return (
    <h5>
      {addOn?.name}&nbsp;
      {hasBadge && <Badge variant={variant}>{status}</Badge>}
      <div className="float-right">{addOn?.type}</div>
    </h5>
  );
};

export const AddOn = ({ addOn, version }) => {
  const link = useMemo(
    () =>
      addOn?.uid
        ? version
          ? `/show/${addOn.uid}?highlightVersion=${version}`
          : `/show/${addOn.uid}`
        : "",
    [addOn, version]
  );

  return (
    <Link to={link}>
      <Media className="addon">
        {addOn && (
          <LegacyFaIcon
            icon={addOn.icon}
            style={addOnItemStyle}
            size={"3x"}
            ifNotFound={<span style={{ ...addOnItemStyle, height: "3rem" }} />}
          />
        )}
        <Media.Body>
          <Title addOn={addOn} />
          <p>{addOn?.description}</p>
        </Media.Body>
      </Media>
    </Link>
  );
};
