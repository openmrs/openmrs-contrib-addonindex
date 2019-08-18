package org.openmrs.addonindex.rest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.util.Version;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecentReleasesControllerIT {

    @LocalServerPort
    private int port;

    @MockBean
    private Index index;

    @Autowired
    private RecentReleasesController controller;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Before
    public void setUp() throws Exception {
        AddOnVersion version = new AddOnVersion();
        version.setVersion(new Version("1.0"));
        version.setReleaseDatetime(OffsetDateTime.parse("2016-09-12T18:51:14.574Z"));
        version.setDownloadUri("http://www.google.com");

        AddOnInfoAndVersions info = new AddOnInfoAndVersions();
        info.setUid("reporting-module");
        info.setModuleId("1");
        info.setModulePackage("org.openmrs.module.reporting-module");
        info.setName("Reporting Module");
        info.setDescription("For reporting");
        info.addVersion(version);

        List<AddOnInfoAndVersions> recent = new ArrayList<>();
        recent.add(info);

        when(index.getRecentReleases(1)).thenReturn(recent);
    }

    @Test
    public void getLatestRelease() throws Exception {
        ResponseEntity<String> entity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/recentreleases?&resultSize=1",
                String.class);
        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        JSONAssert.assertEquals("[{uid:\"reporting-module\","
                        + "name:\"Reporting Module\","
                        + "description:\"For reporting\","
                        + "versionCount:1,"
                        + "latestVersion:\"1.0\","
                        + "versions:[{"
                        + "version:\"1.0\","
                        + "releaseDatetime:\"2016-09-12T18:51:14.574Z\","
                        + "downloadUri:\"http://www.google.com\""
                        + "}]}]",
                entity.getBody(), false);
    }

}