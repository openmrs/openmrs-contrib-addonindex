/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { useState } from "react";
import { Button, Col, Form, FormControl, InputGroup } from "react-bootstrap";
import { useSearchParams } from "../hooks";
import { handleParam } from "../utils";
import { useHistory } from "react-router";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons";

export const SearchBox: React.FC = () => {
  const history = useHistory();
  const { type, q, tag } = useSearchParams<{
    type: string | string[];
    q: string | string[];
    tag: string | string[];
  }>();
  const [query, setQuery] = useState(q);

  return (
    <Form
      className={"row"}
      onSubmit={(evt) => {
        evt.preventDefault();

        const searchParams = new URLSearchParams();
        handleParam("type", type, searchParams);
        handleParam("q", query, searchParams);
        handleParam("tag", tag, searchParams);

        history.push(`/search?${searchParams.toString()}`);
      }}
    >
      <Col md={12} lg={8}>
        <InputGroup size="lg">
          <FormControl
            type="text"
            name="q"
            defaultValue={q}
            placeholder="Search..."
            autoFocus
            onChange={(evt) => setQuery(evt.target.value)}
          />
          <InputGroup.Append>
            <Button type="submit" variant="outline-secondary">
              <FontAwesomeIcon icon={faSearch} />
            </Button>
          </InputGroup.Append>
        </InputGroup>
      </Col>
    </Form>
  );
};
