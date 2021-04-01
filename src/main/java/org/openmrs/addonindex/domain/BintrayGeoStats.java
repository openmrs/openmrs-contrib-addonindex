/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines POJO for parse the Download counts of a module
 * example : {"result":"success","timestamp":1513206041782,"data":{"US":43,"MW":6,"TH":3,"KE":2,"NG":1},
 * "totalIsPublic":true,"advanced":false,"totalDownloads":{"US":43,"MW":6,"TH":3,"KE":2,"NG":1}}
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BintrayGeoStats {

    @JsonProperty("totalDownloads")
    private Map<String, Integer> totalDownloads;

}
