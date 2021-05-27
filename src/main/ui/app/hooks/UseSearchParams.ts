import { useMemo } from "react";
import { useLocation } from "react-router";

// Hook to grab the current URL parameters
export function useSearchParams<
  Params extends { [k in keyof Params]?: string | string[] }
>(): Params {
  const location = useLocation();
  return useMemo(() => {
    const urlParams = new URLSearchParams(location.search);
    const result: Params = {} as Params;
    for (const [key, value] of urlParams) {
      if (key in result) {
        const record = result[key];
        if (record instanceof Array) {
          record.push(value);
        } else {
          result[key] = [record, value];
        }
      } else {
        result[key] = value;
      }
    }

    return result;
    // eslint-disable-next-line  react-hooks/exhaustive-deps
  }, [location.pathname, location.search]);
}
