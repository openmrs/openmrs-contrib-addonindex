package org.openmrs.addonindex.rest;


import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.service.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecentReleasesController {

    private Index index;

    @Autowired
    public RecentReleasesController(Index index) {
        this.index = index;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/recentreleases")
    public List<AddOnInfoAndVersions> getRecentReleases(@RequestParam(
            value = "resultSize", required = false) Integer resultSize) throws Exception {
        return index.getRecentReleases(resultSize);
    }
}
