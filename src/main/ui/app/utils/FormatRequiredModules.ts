/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import type { IAddOnVersion, IModuleRequirement } from "../types";

const describeRequirement = (m: IModuleRequirement) =>
  `${m.module.replace("org.openmrs.module.", "")} ${m.version || ""}`.trim() +
  (m.optional ? " (optional)" : "");

export const formatRequiredModules = (version: IAddOnVersion): string => {
  if (!version.requireModules) {
    return "";
  }

  // required first, so the things you must install lead the list
  const required = version.requireModules.filter((m) => !m.optional);
  const optional = version.requireModules.filter((m) => m.optional);

  return [...required, ...optional].map(describeRequirement).join(", ");
};
