import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { vi, describe, it, expect, beforeEach } from "vitest";
import dayjs from "dayjs/esm";
import relativeTime from "dayjs/esm/plugin/relativeTime";
import localizedFormat from "dayjs/esm/plugin/localizedFormat";
import type { IAddOn } from "./types";

// registered in index.tsx in the real app, which tests never import
dayjs.extend(relativeTime);
dayjs.extend(localizedFormat);

vi.mock("react-ga", () => ({
  default: { initialize: vi.fn(), set: vi.fn(), pageview: vi.fn() },
}));

vi.mock("./component", async (importOriginal) => ({
  ...(await importOriginal<typeof import("./component")>()),
  LegacyFaIcon: () => <span />,
}));

// vi.mock factories are hoisted above every import, so the mock fn must be
// created with vi.hoisted or the factory references it before initialisation
const { myFetch } = vi.hoisted(() => ({ myFetch: vi.fn() }));

vi.mock("./utils", async (importOriginal) => {
  const actual = await importOriginal<typeof import("./utils")>();
  return { ...actual, myFetch };
});

import App from "./App";
import { Show } from "./route/Show";

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

const renderAppWithShow = (addOn: IAddOn) => {
  myFetch.mockImplementation((url: string) => {
    if (url.includes("coreversions")) {
      return Promise.resolve(["2.6.0", "2.7.0"]);
    }
    if (url.includes("/list")) {
      return Promise.resolve([]);
    }
    if (url.includes("latestVersion")) {
      return Promise.resolve(addOn.versions[0]);
    }
    return Promise.resolve(addOn);
  });
  return render(
    <MemoryRouter initialEntries={[`/show/${addOn.uid}`]}>
      <App>
        <Routes>
          <Route path="/show/:uid" element={<Show />} />
        </Routes>
      </App>
    </MemoryRouter>,
  );
};

const pickerColumn = () =>
  screen.getByText("OpenMRS Platform version").closest(".col-sm-3");

describe("the platform picker column in the App header", () => {
  beforeEach(() => {
    myFetch.mockReset();
  });

  it("really gets d-none for a frontend module", async () => {
    renderAppWithShow(frontendApp);
    await waitFor(() =>
      expect(screen.getByText("@openmrs/esm-billing-app")).toBeVisible(),
    );
    // the spy tests in Show.test.tsx verify the setter calls; this verifies the
    // class actually lands on the column, closing the loop through App's context
    await waitFor(() => expect(pickerColumn()).toHaveClass("d-none"));
  });

  it("stays visible for an OMOD", async () => {
    renderAppWithShow(omod);
    await waitFor(() =>
      expect(screen.getByText("Reporting Module")).toBeVisible(),
    );
    expect(pickerColumn()).not.toHaveClass("d-none");
  });
});
