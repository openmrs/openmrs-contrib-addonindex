package org.openmrs.addonindex.scheduled;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openmrs.addonindex.domain.artifactory.VersionList;
import org.openmrs.addonindex.service.artifactory.VersionsService;

import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.openmrs.addonindex.TestUtil.getFileAsString;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FetchCoreVersionsListIT {

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private VersionsService versionsService;

    @Autowired
    private FetchCoreVersionsList fetchCoreVersionsList;

    private VersionList versionList;

    private static final String[] STRINGS_TO_EXCLUDE = {"alpha", "beta", "RC", "SNAPSHOT"};

    @Before
    public void setUp() throws Exception {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
    }

    @Test
    public void testCoreVersionsJsonHandle() throws Exception{
        when(restTemplate.getForObject("https://openmrs.jfrog.io/openmrs/api/storage/public/org/openmrs/api/openmrs-api/",
                String.class))
                .thenReturn(getFileAsString("core-versions.json"));

        fetchCoreVersionsList.fetchCoreVersionsList();
        versionList = versionsService.getVersions();
        assertNotNull(versionList);
        Version testVersion = new Version("1.9.12-SNAPSHOT");
        assertFalse(versionList.getVersions().contains(testVersion));
        assertThat(versionList.getVersions().first().toString(),is("1.6.3"));
    }
}