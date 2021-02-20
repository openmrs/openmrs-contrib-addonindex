/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { findIconDefinition } from "@fortawesome/fontawesome-svg-core";
import { createElement } from "react";

/*
 * LegacyFaIcon - this is small React component designed to map between Bootstrap 3-style FontAwesome class names and
 * modern FontAwesome SVG icons. Probably best to directly use <FontAwesomeIcon/> if possible.
 */
export const LegacyFaIcon = ({ icon, ifNotFound = null, ...props }) => {
  if (!!!icon) {
    return ifNotFound;
  }

  let myIcon;
  if (typeof icon === "string") {
    if (icon.endsWith("-o")) {
      myIcon = findIconDefinition({
        prefix: "far",
        iconName: icon.slice(0, -2),
      });

      if (!!!myIcon) {
        myIcon = findIconDefinition({ iconName: icon });
      }
    } else {
      myIcon = findIconDefinition({ iconName: icon });
    }
  } else {
    myIcon = icon;
  }

  return myIcon
    ? createElement(FontAwesomeIcon, { icon: myIcon, ...props })
    : ifNotFound;
};
