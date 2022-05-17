/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { PropsWithChildren } from "react";
import { Link } from "../types";

interface Props {
  link: string | Link;
}

export const ExternalLink: React.FC<PropsWithChildren<Props>> = ({
  link,
  children,
}) => {
  const myLink =
    typeof link === "string" ? ({ href: link } as Link) : (link as Link);
  const title = myLink.title ? myLink.title : myLink.href;
  return (
    <a target="_blank" href={myLink.href} rel={myLink.rel}>
      {children ? children : title}
    </a>
  );
};
