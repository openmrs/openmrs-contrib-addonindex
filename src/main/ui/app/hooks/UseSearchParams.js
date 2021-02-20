import React, { useMemo } from "react";
import { useLocation } from "react-router";

// straight-forward hook to track the current URL parameters
export const useSearchParams = () => {
  const location = useLocation();
  return useMemo(() => {
    const urlParams = new URLSearchParams(location.search);
    const result = {};
    for (const [key, value] of urlParams) {
      if (key in result) {
        if (Array.isArray(result[key])) {
          result[key].push(value);
        } else {
          result[key] = [result[key], value];
        }
      } else {
        result[key] = value;
      }
    }

    return result;
  }, [location.search]);
};
