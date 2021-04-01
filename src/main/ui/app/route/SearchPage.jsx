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
import { useSearchParams } from "../hooks";
import { useQuery } from "react-query";
import { handleParam, myFetch } from "../utils";
import { AddOnList, SearchBox } from "../component";

export const SearchPage = () => {
  const { type, q, tag } = useSearchParams();

  const searchQuery = useQuery(["search", type, q, tag], () => {
    const searchParams = new URLSearchParams();
    handleParam("type", type, searchParams);
    handleParam("q", q, searchParams);
    handleParam("tag", tag, searchParams);

    return myFetch(`/api/v1/addon?${searchParams.toString()}`);
  });

  const searchResults = useMemo(() => searchQuery.data, [searchQuery.data]);

  return (
    <>
      <SearchBox />
      {searchResults ? (
        <AddOnList
          addOns={searchResults}
          heading={`${searchResults.length} result(s)`}
        />
      ) : searchQuery.isLoading ? (
        <>Searching for {q}...</>
      ) : (
        <>No results</>
      )}
    </>
  );
};
