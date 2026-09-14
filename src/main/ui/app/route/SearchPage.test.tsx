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
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi, describe, it, expect, beforeEach } from "vitest";
import type { IAddOn } from "../types";

vi.mock("../component", async (importOriginal) => ({
  ...(await importOriginal<typeof import("../component")>()),
  LegacyFaIcon: () => <span />,
}));

const { myFetch } = vi.hoisted(() => ({ myFetch: vi.fn() }));

vi.mock("../utils", async (importOriginal) => {
  const actual = await importOriginal<typeof import("../utils")>();
  return { ...actual, myFetch };
});

import { SearchPage } from "./SearchPage";

const results: IAddOn[] = [
  { uid: "reporting", name: "Reporting Module", type: "OMOD", hostedUrl: "" },
  {
    uid: "patient-app",
    name: "Patient App",
    type: "FRONTEND_MODULE",
    hostedUrl: "",
  },
  {
    uid: "hiv-content",
    name: "HIV Content",
    type: "CONTENT_PACKAGE",
    hostedUrl: "",
  },
];

const renderSearch = (search: string) => {
  myFetch.mockResolvedValue(results);
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[`/search?${search}`]}>
        <Routes>
          <Route path="/search" element={<SearchPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
};

beforeEach(() => {
  myFetch.mockReset();
});

describe("<SearchPage/>", () => {
  it("asks the API for every add-on type", async () => {
    renderSearch("q=patient");
    await waitFor(() => expect(myFetch).toHaveBeenCalled());
    const url: string = myFetch.mock.calls[0][0];
    for (const type of ["OMOD", "OWA", "CONTENT_PACKAGE", "FRONTEND_MODULE"]) {
      expect(url).toContain(`type=${type}`);
    }
    expect(url).toContain("q=patient");
  });

  it("shows a pill with a count for each type", async () => {
    renderSearch("q=patient");
    expect(await screen.findByText("All (3)")).toBeVisible();
    expect(screen.getByText("Backend Modules (1)")).toBeVisible();
    expect(screen.getByText("Frontend Modules (1)")).toBeVisible();
    expect(screen.getByText("Content Packages (1)")).toBeVisible();
    expect(screen.getByText("Open Web Apps (0)")).toBeVisible();
    expect(screen.getByText("3 result(s)")).toBeVisible();
  });

  it("filters the results by the selected pill without refetching", async () => {
    renderSearch("q=patient");
    fireEvent.click(await screen.findByText("Content Packages (1)"));

    expect(await screen.findByText("1 result(s)")).toBeVisible();
    expect(screen.getByText("HIV Content")).toBeVisible();
    expect(screen.queryByText("Reporting Module")).not.toBeInTheDocument();
    expect(myFetch).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByText("All (3)"));
    expect(await screen.findByText("3 result(s)")).toBeVisible();
  });

  it("honours a type already in the URL", async () => {
    renderSearch("type=OMOD&q=patient");
    expect(await screen.findByText("1 result(s)")).toBeVisible();
    expect(screen.getByText("Reporting Module")).toBeVisible();
  });
});
