/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.backend;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryArtifactDetails;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfo;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryFolderInfoChild;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * A {@link BackendHandler} that lists versions of an artifact in OpenMRS's Artifactory "releases"
 * repo from its folder listing, rather than searching for files the way {@link Artifactory} does.
 * Each version folder is assumed to hold {@code (artifactId)-(version).(type's file extension)}.
 * </p>
 * <p>
 * This suits artifacts with many files per version (e.g. openmrs-webapp's war, pom, jar and
 * signatures), where Artifactory's public search API truncates at 1000 results and drops versions.
 * Listing costs one request; each version's release date costs one more, which
 * {@link #fetchVersionDetails} only spends on versions not already indexed.
 * </p>
 */
@Component
public class ArtifactoryVersionFolders implements SupportsVersionDetails {
	
	private static final String REPO = "releases";
	
	private static final String[] STRINGS_TO_EXCLUDE = { "alpha", "beta", "RC", "SNAPSHOT" };
	
	private final RestTemplate restTemplate;
	
	@Autowired
	public ArtifactoryVersionFolders(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		if (addOnToIndex.getMavenRepoDetails() == null) {
			throw new IllegalStateException("No maven repo details provided for AddOn: " + addOnToIndex.getName());
		}
		
		ArtifactoryFolderInfo folderInfo = restTemplate.getForObject(storageUrl(addOnToIndex) + "/",
		    ArtifactoryFolderInfo.class);
		if (folderInfo == null || folderInfo.getChildren() == null) {
			throw new IllegalStateException("No folder listing returned for " + storageUrl(addOnToIndex));
		}
		
		AddOnInfoAndVersions result = AddOnInfoAndVersions.from(addOnToIndex);
		for (ArtifactoryFolderInfoChild child : folderInfo.getChildren()) {
			if (!child.getFolder() || Arrays.stream(STRINGS_TO_EXCLUDE).anyMatch(child.getUri()::contains)) {
				continue;
			}
			
			String version = child.getUri().replace("/", "");
			AddOnVersion addOnVersion = new AddOnVersion();
			addOnVersion.setVersion(new Version(version));
			addOnVersion.setDownloadUri(
			    String.join("/", Artifactory.ARTIFACTORY_URL, REPO, artifactPath(addOnToIndex, version)));
			addOnVersion.setRenameTo(
			    addOnToIndex.getType() == AddOnType.CORE ? "openmrs.war" : fileNameFor(addOnToIndex, version));
			result.getVersions().add(addOnVersion);
		}
		
		Collections.sort(result.getVersions());
		return result;
	}
	
	/**
	 * Sets the version's release date from Artifactory's storage API. Throws if the version folder has
	 * no file of the expected name, so the version is left out of the index.
	 */
	@Override
	public void fetchVersionDetails(AddOnToIndex toIndex, AddOnVersion addOnVersion) throws Exception {
		String version = addOnVersion.getVersion().toString();
		String url = String.join("/", Artifactory.ARTIFACTORY_URL, "api/storage", REPO, artifactPath(toIndex, version));
		
		ArtifactoryArtifactDetails details = restTemplate.getForObject(url, ArtifactoryArtifactDetails.class);
		if (details == null || details.getCreated() == null) {
			throw new IllegalStateException("No artifact details returned for " + url);
		}
		
		addOnVersion.setReleaseDatetime(OffsetDateTime.parse(details.getCreated()));
	}
	
	private static String storageUrl(AddOnToIndex addOnToIndex) {
		return String.join("/", Artifactory.ARTIFACTORY_URL, "api/storage", REPO, artifactFolder(addOnToIndex));
	}
	
	private static String artifactFolder(AddOnToIndex addOnToIndex) {
		String groupPath = addOnToIndex.getMavenRepoDetails().getGroupId().replace(".", "/").toLowerCase(Locale.ROOT);
		return groupPath + "/" + addOnToIndex.getMavenRepoDetails().getArtifactId().toLowerCase(Locale.ROOT);
	}
	
	private static String artifactPath(AddOnToIndex addOnToIndex, String version) {
		return String.join("/", artifactFolder(addOnToIndex), version, fileNameFor(addOnToIndex, version));
	}
	
	private static String fileNameFor(AddOnToIndex addOnToIndex, String version) {
		return addOnToIndex.getMavenRepoDetails().getArtifactId().toLowerCase(Locale.ROOT) + "-" + version + "."
		        + addOnToIndex.getType().getFileExtension();
	}
}
