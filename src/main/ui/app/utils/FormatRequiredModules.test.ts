import { describe, it, expect } from "vitest";
import { formatRequiredModules } from "./FormatRequiredModules";
import type { IAddOnVersion } from "../types";

const version = (requireModules: IAddOnVersion["requireModules"]) =>
  ({ requireModules }) as IAddOnVersion;

describe("formatRequiredModules", () => {
  it("returns an empty string when there are no requirements", () => {
    expect(formatRequiredModules(version(undefined))).toBe("");
  });

  it("strips the org.openmrs.module prefix from OMOD requirements", () => {
    expect(
      formatRequiredModules(
        version([{ module: "org.openmrs.module.reporting", version: "1.2.3" }]),
      ),
    ).toBe("reporting 1.2.3");
  });

  it("leaves backend module ids untouched", () => {
    expect(
      formatRequiredModules(
        version([{ module: "webservices.rest", version: ">=2.24.0" }]),
      ),
    ).toBe("webservices.rest >=2.24.0");
  });

  it("joins multiple requirements", () => {
    expect(
      formatRequiredModules(
        version([
          { module: "org.openmrs.module.billing", version: "2.3.0" },
          { module: "webservices.rest", version: "2.24.0" },
        ]),
      ),
    ).toBe("billing 2.3.0, webservices.rest 2.24.0");
  });
});
