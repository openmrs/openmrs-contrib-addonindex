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
import { useNavigate } from "react-router";
import { Button } from "react-bootstrap";
import { useSearchParams } from "../hooks";
import { useQuery } from "@tanstack/react-query";
import { handleParam, myFetch } from "../utils";
import { AddOnList, SearchBox } from "../component";
import { IAddOn } from "../types";

// pill order; a total Record so a new AddOnType fails to compile here
const TYPE_LABELS: Record<IAddOn["type"], string> = {
  OMOD: "Backend Modules",
  FRONTEND_MODULE: "Frontend Modules",
  CONTENT_PACKAGE: "Content Packages",
  OWA: "Open Web Apps",
};

// the API defaults to OMOD and OWA only, so the website asks for every type explicitly
const ALL_TYPES = Object.keys(TYPE_LABELS) as IAddOn["type"][];

const TypePill: React.FC<{
  active: boolean;
  label: string;
  count: number;
  onClick: () => void;
}> = ({ active, label, count, onClick }) => (
  <Button
    size="sm"
    variant={active ? "primary" : "outline-primary"}
    className="rounded-pill mr-2"
    aria-pressed={active}
    onClick={onClick}
  >
    {label} ({count})
  </Button>
);

export const SearchPage: React.FC = () => {
  const navigate = useNavigate();
  const { type, q, tag } = useSearchParams<{
    type: string | string[];
    q: string | string[];
    tag: string | string[];
  }>();

  const searchQuery = useQuery({
    queryKey: ["search", q, tag],
    queryFn: () => {
      const searchParams = new URLSearchParams();
      handleParam("type", ALL_TYPES, searchParams);
      handleParam("q", q, searchParams);
      handleParam("tag", tag, searchParams);

      return myFetch<IAddOn[]>(`/api/v1/addon?${searchParams.toString()}`);
    },
  });

  const selectedTypes = useMemo(() => (type ? [type].flat() : []), [type]);

  const searchResults = useMemo(
    () =>
      searchQuery.data?.filter(
        (addOn) =>
          selectedTypes.length === 0 || selectedTypes.includes(addOn.type),
      ),
    [searchQuery.data, selectedTypes],
  );

  const countByType = useMemo(
    () =>
      (searchQuery.data ?? []).reduce(
        (counts, addOn) => {
          counts[addOn.type] = (counts[addOn.type] ?? 0) + 1;
          return counts;
        },
        {} as Partial<Record<IAddOn["type"], number>>,
      ),
    [searchQuery.data],
  );

  const selectType = (selected?: IAddOn["type"]) => {
    const searchParams = new URLSearchParams();
    handleParam("type", selected, searchParams);
    handleParam("q", q, searchParams);
    handleParam("tag", tag, searchParams);
    navigate(`/search?${searchParams.toString()}`);
  };

  return (
    <>
      <SearchBox />
      {searchQuery.data && (
        <div className="my-3">
          <strong className="mr-2">Type:</strong>
          <TypePill
            active={selectedTypes.length === 0}
            label="All"
            count={searchQuery.data.length}
            onClick={() => selectType()}
          />
          {ALL_TYPES.map((t) => (
            <TypePill
              key={t}
              active={selectedTypes.length === 1 && selectedTypes[0] === t}
              label={TYPE_LABELS[t]}
              count={countByType[t] ?? 0}
              onClick={() => selectType(t)}
            />
          ))}
        </div>
      )}
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
