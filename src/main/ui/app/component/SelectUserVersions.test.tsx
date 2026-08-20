import React from "react";
import { render, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi, describe, it, expect, beforeEach } from "vitest";

// vi.mock factories are hoisted above every import, so the mock fn must be
// created with vi.hoisted or the factory references it before initialisation
const { myFetch } = vi.hoisted(() => ({ myFetch: vi.fn() }));

vi.mock("../utils", async (importOriginal) => {
  const actual = await importOriginal<typeof import("../utils")>();
  return { ...actual, myFetch };
});

import { SelectUserVersions } from "./SelectUserVersions";

describe("<SelectUserVersions/>", () => {
  beforeEach(() => {
    myFetch.mockReset();
  });

  it("does not corrupt React Query's cached version list across remounts", async () => {
    myFetch.mockResolvedValue(["1.0.0", "2.0.0", "3.0.0"]);
    const client = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    });
    const ui = (
      <QueryClientProvider client={client}>
        <SelectUserVersions updateValue={() => undefined} />
      </QueryClientProvider>
    );

    const first = render(ui);
    await waitFor(() =>
      expect(client.getQueryData(["coreversions"])).toBeDefined(),
    );
    first.unmount();
    render(ui);

    // the component shows versions newest-first by reversing a COPY; an in-place
    // reverse would flip the cached array itself on every remount
    await waitFor(() =>
      expect(client.getQueryData(["coreversions"])).toEqual([
        "1.0.0",
        "2.0.0",
        "3.0.0",
      ]),
    );
  });
});
