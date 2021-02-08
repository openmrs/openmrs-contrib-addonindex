package org.openmrs.addonindex.scheduled;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfo;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfoChild;
import org.openmrs.addonindex.service.VersionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@Slf4j
public class FetchCoreVersionsList {

    private static final String[] STRINGS_TO_EXCLUDE = {"alpha", "beta", "RC", "SNAPSHOT"};

    @Value("${core_version_list.url}")
    private String url;

    @Value("${core_version_list.strategy}")
    private final FetchCoreVersionsList.Strategy strategy = FetchCoreVersionsList.Strategy.FETCH;

    private final RestTemplateBuilder restTemplateBuilder;

    private final ObjectMapper objectMapper;

    private final VersionsService versionsService;

    @Autowired
    public FetchCoreVersionsList(RestTemplateBuilder restTemplateBuilder,
                                 ObjectMapper objectMapper,
                                 VersionsService versionsService) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.objectMapper = objectMapper;
        this.versionsService = versionsService;
    }

    @Scheduled(
            initialDelayString = "${scheduler.fetch_core_versions_list.initial_delay}",
            fixedDelayString = "${scheduler.fetch_core_versions_list.period}")
    public void fetchCoreVersionsList() throws Exception {
        log.info("Fetching list of OpenMRS-Core versions to index");

        String json;
        if (strategy == Strategy.LOCAL) {
            log.debug("LOCAL strategy");
            json = StreamUtils.copyToString(getClass().getClassLoader().getResourceAsStream("openmrs-core-versions.json"),
                    Charset.defaultCharset());
        } else {
            log.debug("FETCH strategy: " + url);
            json = restTemplateBuilder.build().getForObject(url, String.class);
        }

        ArtifactoryFolderInfo versionlist;
        try {
            versionlist = objectMapper.readValue(json, ArtifactoryFolderInfo.class);
        } catch (Exception ex) {
            throw new IllegalStateException("File downloaded from " + url + " could not be parsed", ex);
        }

        if (log.isInfoEnabled()) {
            log.info("There are {} openmrs-core versions", versionlist.getChildren().size());
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
            log.warn("File downloaded from {} does not list any Core Versions to index. Keeping our current list", url);
        }
    }

    private static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).parallel().anyMatch(inputStr::contains);
    }

    public enum Strategy {
        FETCH, LOCAL
    }
}
