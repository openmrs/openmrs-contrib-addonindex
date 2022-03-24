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
import { IAddOn } from "../types";

export const SearchPage: React.FC = () => {
  const { type, q, tag } = useSearchParams<{
    type: string | string[];
    q: string | string[];
    tag: string | string[];
  }>();

  const searchQuery = useQuery<IAddOn[]>(["search", type, q, tag], () => {
    const searchParams = new URLSearchParams();
    handleParam("type", type, searchParams);
    handleParam("q", q, searchParams);
    handleParam("tag", tag, searchParams);

    return myFetch<IAddOn[]>(`/api/v1/addon?${searchParams.toString()}`);
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
