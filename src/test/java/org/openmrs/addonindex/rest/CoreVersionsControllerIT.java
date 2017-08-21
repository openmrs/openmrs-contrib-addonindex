package org.openmrs.addonindex.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.artifactory.VersionList;
import org.openmrs.addonindex.rest.artifactory.CoreVersionsController;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.service.artifactory.VersionsService;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CoreVersionsControllerIT {
    @LocalServerPort
    private int port;

    @MockBean
    private Index index;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private VersionsService versionsService;

    @Autowired
    private CoreVersionsController controller;

    @Before
    public void setUp() throws Exception {
        List<String> versions = new ArrayList<>();
        versions.add("1.6.3");
        versions.add("1.6.4");
        VersionList versionList;
        versionList = new VersionList(versions);
        doReturn(versionList).when(versionsService).getVersions();
    }

    @Test
    public void getCoreversions() throws Exception {
        ResponseEntity<String> entity = testRestTemplate.getForEntity("http://localhost:" + port + "/api/v1/coreversions",
                String.class);

        assertThat(entity.getStatusCode(), is(HttpStatus.OK));
        JSONAssert.assertEquals("[\"1.6.3\",\"1.6.4\"]",entity.getBody(), false);
    }
}