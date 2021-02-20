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
import { Link } from "react-router-dom";
import { ExternalLink } from "../component";

export const About = () => {
  return (
    <>
      <h1>About</h1>
      <h3>How to add your module?</h3>
      <p>
        Adding your module to Add Ons for indexing is as easy as creating a pull
        request! First you need to upload your module to Bintray, and then you
        may head over to the{" "}
        <ExternalLink link="https://github.com/openmrs/openmrs-contrib-addonindex/blob/master/PUBLISHING-AN-ADD-ON.md">
          Publishing an Add-on document
        </ExternalLink>{" "}
        on Github which contains a comprehensive list of steps.
      </p>
      <p>
        Discussions on the working of this tool and/or proposal of new features
        may be done on OpenMRS Talk in the{" "}
        <ExternalLink link="https://talk.openmrs.org/c/projects/add-on-index">
          projects:add-on-index
        </ExternalLink>{" "}
        category.
      </p>
      <h3>About the OpenMRS Add Ons Tool</h3>
      <ul>
        <li>
          <strong>Contributing:</strong> You can find the source code for Add
          Ons{" "}
          <ExternalLink link="https://github.com/openmrs/openmrs-contrib-addonindex">
            here
          </ExternalLink>
          ; for more details, please see the{" "}
          <ExternalLink link="https://github.com/openmrs/openmrs-contrib-addonindex/blob/master/CONTRIBUTING.md">
            CONTRIBUTING.md document
          </ExternalLink>
        </li>
        <li>
          <strong>Discussion Forum:</strong> The discussion forum for OpenMRS
          Add Ons is located{" "}
          <ExternalLink link="https://talk.openmrs.org/c/projects/add-on-index">
            here
          </ExternalLink>
        </li>
        <li>
          <strong>License:</strong> This software is available under the{" "}
          <ExternalLink link="http://openmrs.org/license/">
            Mozilla Public License 2.0 with Healthcare Disclaimer (MPL 2.0 HD)
          </ExternalLink>
          .
          <br />
          "OpenMRS" is a registered trademark and the OpenMRS graphic logo is a
          trademark of OpenMRS Inc.
        </li>
      </ul>
      <h3>Stats</h3>
      <p>
        <Link to="/indexingStatus">Indexing Status</Link>
      </p>
    </>
  );
};
