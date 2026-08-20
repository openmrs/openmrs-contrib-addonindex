import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi, describe, it, expect, beforeEach } from "vitest";
import dayjs from "dayjs/esm";
import relativeTime from "dayjs/esm/plugin/relativeTime";
import localizedFormat from "dayjs/esm/plugin/localizedFormat";
import type { IAddOn } from "../types";

// registered in index.tsx in the real app, which tests never import
dayjs.extend(relativeTime);
dayjs.extend(localizedFormat);

vi.mock("../component", async (importOriginal) => ({
  ...(await importOriginal<typeof import("../component")>()),
  LegacyFaIcon: () => <span />,
}));

// vi.mock factories are hoisted above every import, so the mock fn must be
// created with vi.hoisted or the factory references it before initialisation
const { myFetch } = vi.hoisted(() => ({ myFetch: vi.fn() }));

vi.mock("../utils", async (importOriginal) => {
  const actual = await importOriginal<typeof import("../utils")>();
  return { ...actual, myFetch };
});

import { Show } from "./Show";
import { HidePlatformPickerContext } from "../App";

const frontendApp: IAddOn = {
  uid: "openmrs-esm-billing-app",
  name: "@openmrs/esm-billing-app",
  type: "FRONTEND_MODULE",
  hostedUrl: "",
  versions: [
    {
      version: "1.3.1",
      downloadUri:
        "https://registry.npmjs.org/@openmrs/esm-billing-app/-/esm-billing-app-1.3.1.tgz",
      requireModules: [
        { module: "org.openmrs.module.billing", version: "2.3.0" },
        { module: "webservices.rest", version: "2.24.0" },
      ],
    },
  ],
};

const omod: IAddOn = {
  uid: "reporting",
  name: "Reporting Module",
  type: "OMOD",
  hostedUrl: "",
  versions: [
    {
      version: "1.0.0",
      downloadUri: "https://example.org/reporting-1.0.0.omod",
      requireOpenmrsVersion: "2.6.0",
    },
  ],
};

const renderShow = (addOn: IAddOn) => {
  const setHidePlatformPicker = vi.fn();
  myFetch.mockImplementation((url: string) =>
    url.includes("latestVersion")
      ? Promise.resolve(addOn.versions[0])
      : Promise.resolve(addOn),
  );
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const utils = render(
    <QueryClientProvider client={client}>
      <HidePlatformPickerContext.Provider value={setHidePlatformPicker}>
        <MemoryRouter initialEntries={[`/show/${addOn.uid}`]}>
          <Routes>
            <Route path="/show/:uid" element={<Show />} />
          </Routes>
        </MemoryRouter>
      </HidePlatformPickerContext.Provider>
    </QueryClientProvider>,
  );
  return { setHidePlatformPicker, ...utils };
};

beforeEach(() => {
  myFetch.mockReset();
});

describe("the platform picker", () => {
  it("is hidden for a frontend module and restored on leaving the page", async () => {
    const { setHidePlatformPicker, unmount } = renderShow(frontendApp);
    await waitFor(() =>
      expect(setHidePlatformPicker).toHaveBeenCalledWith(true),
    );
    setHidePlatformPicker.mockClear();
    // without the effect's cleanup, the picker would stay hidden on every later page
    unmount();
    expect(setHidePlatformPicker).toHaveBeenCalledWith(false);
  });

  it("is never hidden for an OMOD", async () => {
    const { setHidePlatformPicker } = renderShow(omod);
    await waitFor(() =>
      expect(screen.getByText("Reporting Module")).toBeVisible(),
    );
    expect(setHidePlatformPicker).not.toHaveBeenCalledWith(true);
  });
});

describe("<Show/> for a frontend module", () => {
  it("hides the Platform Requirements column", async () => {
    renderShow(frontendApp);
    await waitFor(() =>
      expect(screen.getByText("@openmrs/esm-billing-app")).toBeVisible(),
    );
    expect(screen.queryByText(/Platform Requirements/)).not.toBeInTheDocument();
  });

  it("shows backend dependencies", async () => {
    renderShow(frontendApp);
    await waitFor(() =>
      expect(
        screen.getByText("billing 2.3.0, webservices.rest 2.24.0"),
      ).toBeVisible(),
    );
  });

  it("keeps the Platform Requirements column for an OMOD", async () => {
    renderShow(omod);
    await waitFor(() =>
      expect(screen.getByText("Reporting Module")).toBeVisible(),
    );
    expect(screen.getByText(/Platform Requirements/)).toBeVisible();
    expect(screen.getByText("2.6.0")).toBeVisible();
  });
});
