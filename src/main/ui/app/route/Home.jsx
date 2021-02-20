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
import { NamedList, SearchBox } from "../component";
import { useQuery } from "react-query";
import { myFetch } from "../utils";

export const Home = () => {
  const defaultListQuery = useQuery(["defaultList"], () =>
    myFetch("/api/v1/list/DEFAULT")
  );

  const defaultList = useMemo(() => defaultListQuery.data, [
    defaultListQuery.data,
  ]);

  return (
    <>
      <SearchBox />
      {defaultListQuery.isLoading ? (
        <>Loading...</>
      ) : (
        <NamedList list={defaultList} />
      )}
    </>
  );
};
