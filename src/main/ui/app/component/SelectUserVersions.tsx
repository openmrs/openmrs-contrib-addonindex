/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React, { useContext, useMemo } from "react";
import Select from "react-select";
import { useQuery } from "react-query";
import { CoreVersionContext } from "../App";
import { myFetch } from "../utils";

interface Props {
  updateValue: (string) => void;
}

interface Version {
  value: string;
  label: string;
}

export const SelectUserVersions: React.FC<Props> = ({
  updateValue = () => null,
}) => {
  const selectedValue = useContext<string | undefined>(CoreVersionContext);
  const versionQuery = useQuery<string[]>(["coreversions"], () =>
    myFetch<string[]>("/api/v1/coreversions")
  );

  const versions: Version[] = useMemo(() => {
    if (!versionQuery.data) {
      return null;
    }

    return versionQuery.data.reverse().map((v) => ({
      value: v,
      label: v,
    }));
  }, [versionQuery.data]);

  return (
    <>
      OpenMRS Platform version
      {versionQuery.isLoading ? (
        <div>Loading...</div>
      ) : (
        !versionQuery.isError && (
          <Select
            styles={{
              menu: (provided) => ({
                ...provided,
                zIndex: 500,
              }),
            }}
            theme={(theme) => ({
              ...theme,
              colors: {
                ...theme.colors,
                primary: "#337ab7",
                primary75: "#337ab775",
                primary50: "#337ab750",
                primary25: "#337ab725",
              },
            })}
            value={
              selectedValue
                ? { value: selectedValue, label: selectedValue }
                : null
            }
            placeholder="to check compatibility"
            options={versions}
            name="selected-state"
            onChange={(newValue: Version) => updateValue(newValue?.value)}
            isSearchable={true}
          />
        )
      )}
    </>
  );
};
