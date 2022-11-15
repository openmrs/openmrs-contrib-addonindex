import type { Config } from "jest";

const config: Config = {
  testEnvironment: "jsdom",
  transform: {
    "\\.[jt]sx?$": "@swc/jest",
  },
};

export default config;
