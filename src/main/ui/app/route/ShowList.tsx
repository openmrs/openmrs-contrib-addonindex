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
import { useParams } from "react-router";
import { useQuery } from "react-query";
import { NamedList } from "../component";
import { myFetch } from "../utils";
import { AddOnCollection } from "../types";

export const ShowList: React.FC = () => {
  const { uid } = useParams<{ uid: string }>();

  const listQuery = useQuery<AddOnCollection>(["list", uid], () =>
    myFetch<AddOnCollection>(`/api/v1/list/${uid}`)
  );

  const list = useMemo(() => {
    if (!listQuery.data) {
      return null;
    }

    return listQuery.data;
  }, [listQuery]);

  if (listQuery.isError) {
    // TODO is this the right thing to do?
    return <>{listQuery.error.toString()}</>;
  }

  if (listQuery.isLoading || !list) {
    return <>Loading...</>;
  }

  return <NamedList list={list} />;
};
