/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

// simple wrapper for `fetch` that throws on error and returns the json response
// useful to integrate `fetch` with the react-query library
export const myFetch = async <T>(
  url: RequestInfo,
  options: RequestInit = {}
): Promise<T | null> => {
  const response = await fetch(url, options);
  if (!response.ok) throw response;
  if (response.status === 204) {
    return null;
  }
  return await response.json();
};
