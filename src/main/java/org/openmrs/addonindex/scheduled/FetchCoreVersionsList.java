package org.openmrs.addonindex.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfoChild;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfo;
import org.openmrs.addonindex.service.artifactory.VersionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FetchCoreVersionsList {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String[] STRINGS_TO_EXCLUDE = {"alpha", "beta", "RC", "SNAPSHOT"};

    @Value("${core_version_list.url}")
    private String url;

    @Value("${core_version_list.strategy}")
    private FetchCoreVersionsList.Strategy strategy = FetchCoreVersionsList.Strategy.FETCH;

    private RestTemplateBuilder restTemplateBuilder;

    private ObjectMapper mapper;

    private VersionsService versionsService;

    @Autowired
    public FetchCoreVersionsList(RestTemplateBuilder restTemplateBuilder,
                                 ObjectMapper mapper,
                                 VersionsService versionsService) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.mapper = mapper;
        this.versionsService = versionsService;
    }

    @Scheduled(
            initialDelayString = "${scheduler.fetch_core_versions_list.initial_delay}",
            fixedDelayString = "${scheduler.fetch_core_versions_list.period}")
    public void fetchCoreVersionsList() throws Exception {
        logger.info("Fetching list of OpenMRS-Core versions to index");

        String json;
        if (strategy == Strategy.LOCAL) {
            logger.debug("LOCAL strategy");
            json = StreamUtils.copyToString(getClass().getClassLoader().getResourceAsStream("openmrs-core-versions.json"),
                    Charset.defaultCharset());
        } else {
            logger.debug("FETCH strategy: " + url);
            json = restTemplateBuilder.build().getForObject(url, String.class);
        }
        ArtifactoryFolderInfo versionlist;
        try {
            versionlist = mapper.readValue(json, ArtifactoryFolderInfo.class);
        } catch (Exception ex) {
            throw new IllegalStateException("File downloaded from " + url + " could not be parsed", ex);
        }
        if (logger.isInfoEnabled()) {
            logger.info("There are " + versionlist.getChildren().size() + " openmrs-core versions");
        }
        if (versionlist.size() > 0) {
            List<String> versions = new ArrayList<>();
            List<ArtifactoryFolderInfoChild> allversions = versionlist.getChildren();
            for (ArtifactoryFolderInfoChild candidateVersion : allversions) {
                if (candidateVersion.getFolder() && !stringContainsItemFromList(candidateVersion.getUri(), STRINGS_TO_EXCLUDE)) {
                    versions.add(candidateVersion.getUri().replaceAll("/", ""));
                }
            }
            versionsService.setVersions(versions);
        } else {
            logger.warn("File downloaded from " + url + " does not list any Core Versions to index. Keeping our current list");
        }
    }

    public boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }

    public enum Strategy {
        FETCH, LOCAL
    }


}
