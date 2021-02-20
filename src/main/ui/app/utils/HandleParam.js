/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

/*
 * handleParam() - small utility to add a parameter to a URLSearchParams object where the value might be a single value
 * or an array of values. This is useful when combined with the useSearchParams hook.
 */
export const handleParam = (name, value, searchParams) => {
  if (value) {
    if (Array.isArray(value)) {
      value.forEach((v) => searchParams.append(name, v));
    } else {
      searchParams.append(name, value);
    }
  }
};
