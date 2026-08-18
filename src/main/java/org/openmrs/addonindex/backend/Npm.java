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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.npm.NpmDownloadCount;
import org.openmrs.addonindex.domain.npm.NpmPackument;
import org.openmrs.addonindex.domain.npm.NpmVersionInfo;
import org.openmrs.addonindex.util.Version;
import org.openmrs.addonindex.util.VersionRangeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link BackendHandler} for OpenMRS frontend modules published to the npm registry.
 */
@Component
@Slf4j
public class Npm implements BackendHandler, SupportsDownloadCounts, SupportsVersionDetails {
	
	protected static final String REGISTRY_URL = "https://registry.npmjs.org/{package}";
	
	protected static final String DOWNLOADS_URL = "https://api.npmjs.org/downloads/point/last-month/{package}";
	
	protected static final String UNPKG_ROUTES_URL = "https://unpkg.com/{package}@{version}/dist/routes.json";
	
	protected static final String UNPKG_PACKAGE_JSON_URL = "https://unpkg.com/{package}@{version}/package.json";
	
	/**
	 * routes.json refers to backend modules by their module id (e.g. webservices.rest), but the rest of
	 * the index records requirements under the module package (e.g.
	 * org.openmrs.module.webservices.rest). We assume the conventional package prefix, the same
	 * assumption content.properties indexing makes when a dependency declares no groupId.
	 */
	private static final String BACKEND_MODULE_PREFIX = "org.openmrs.module.";
	
	private final RestTemplate restTemplate;
	
	private final ObjectMapper objectMapper;
	
	/**
	 * The last ETag we saw per package, with the versions and description we read from that packument.
	 * A packument lists every version ever published, including a CI pre-release per merge, so it can
	 * run to several MB of which we keep a handful of stable versions. Sending the ETag back as
	 * If-None-Match lets npm answer 304 with an empty body whenever nothing has been published since,
	 * which skips both the download and the parse.
	 */
	private final Map<String, CachedPackument> cachedPackuments = new ConcurrentHashMap<>();
	
	@Autowired
	public Npm(RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}
	
	/**
	 * Everything we keep from one entry of a packument. The indexing run mutates each AddOnVersion it
	 * is handed (see {@link AddOnVersion#addRequiredModule}), so caching AddOnVersions across runs
	 * would accumulate duplicate requirements. Instead we cache these immutable records and rebuild
	 * fresh AddOnVersions from them on every run.
	 */
	private record PublishedVersion(String version, String tarball, OffsetDateTime published) {
	}
	
	private record CachedPackument(String etag, List<PublishedVersion> versions, String description) {

		private CachedPackument {
			// the cache must never hand out a list a caller could mutate
			versions = List.copyOf(versions);
		}
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String npmPackage = addOnToIndex.getNpmPackage();
		if (!StringUtils.hasText(npmPackage)) {
			throw new IllegalStateException("No npm package provided for AddOn: " + addOnToIndex.getName());
		}
		
		AddOnInfoAndVersions result = AddOnInfoAndVersions.from(addOnToIndex);
		
		CachedPackument published = fetchPackument(npmPackage);
		
		// a hand-written description in add-ons-to-index.json wins over the packument's
		if (!StringUtils.hasText(result.getDescription())) {
			result.setDescription(published.description());
		}
		
		SortedSet<AddOnVersion> versions = new TreeSet<>();
		for (PublishedVersion publishedVersion : published.versions()) {
			versions.add(toAddOnVersion(publishedVersion));
		}
		
		result.getVersions().addAll(versions);
		
		return result;
	}
	
	/**
	 * Returns the stable versions and description the registry has published. Answers from
	 * {@link #cachedPackuments} when the registry reports the packument unchanged. Throws when the
	 * registry gives us nothing usable: an empty result indexed as a success would replace the whole
	 * document with nothing.
	 */
	private CachedPackument fetchPackument(String npmPackage) {
		CachedPackument cached = cachedPackuments.get(npmPackage);
		
		HttpHeaders headers = new HttpHeaders();
		if (cached != null) {
			headers.setIfNoneMatch(cached.etag());
		}
		ResponseEntity<NpmPackument> response = restTemplate.exchange(REGISTRY_URL, HttpMethod.GET,
		    new HttpEntity<>(headers), NpmPackument.class, Map.of("package", npmPackage));
		
		if (response.getStatusCode() == HttpStatus.NOT_MODIFIED && cached != null) {
			log.debug("Packument for {} unchanged, reusing {} cached versions", npmPackage, cached.versions().size());
			return cached;
		}
		
		NpmPackument packument = response.getBody();
		if (packument == null || packument.getVersions() == null) {
			throw new IllegalStateException("No packument returned for " + npmPackage);
		}
		
		List<PublishedVersion> published = publishedVersions(npmPackage, packument);
		if (published.isEmpty()) {
			// thrown before the cache put: an empty list cached under this ETag would be replayed
			// by every 304 until the package publishes again
			throw new IllegalStateException("No stable versions found in the packument for " + npmPackage);
		}
		String etag = response.getHeaders().getETag();
		CachedPackument fetched = new CachedPackument(etag, published, packument.getDescription());
		if (etag != null) {
			cachedPackuments.put(npmPackage, fetched);
		}
		return fetched;
	}
	
	private List<PublishedVersion> publishedVersions(String npmPackage, NpmPackument packument) {
		List<PublishedVersion> published = new ArrayList<>();
		for (Map.Entry<String, NpmVersionInfo> entry : packument.getVersions().entrySet()) {
			String versionString = entry.getKey();
			// A hyphen is semver's pre-release marker, so this drops every pre-release (-pre, -rc,
			// -beta, ...) and keeps plain releases only. The per-merge CI builds like 1.3.2-pre.1928
			// are by far the most common case.
			if (versionString.contains("-")) {
				continue;
			}
			
			NpmVersionInfo info = entry.getValue();
			if (info == null || info.getDist() == null || !StringUtils.hasText(info.getDist().getTarball())) {
				log.warn("No tarball for {}@{}", npmPackage, versionString);
				continue;
			}
			
			String publishedAt = packument.getTime() == null ? null : packument.getTime().get(versionString);
			published.add(new PublishedVersion(versionString, info.getDist().getTarball(),
			        parsePublishTime(npmPackage, versionString, publishedAt)));
		}
		return published;
	}
	
	/**
	 * Parsed here, before the value can reach the cache: a string that only failed to parse later would
	 * fail the whole package on every cached 304 until its next publish changes the ETag.
	 */
	private OffsetDateTime parsePublishTime(String npmPackage, String versionString, String publishedAt) {
		if (!StringUtils.hasText(publishedAt)) {
			return null;
		}
		try {
			return OffsetDateTime.parse(publishedAt);
		}
		catch (DateTimeParseException ex) {
			log.warn("Unparseable publish time '{}' for {}@{}", publishedAt, npmPackage, versionString);
			return null;
		}
	}
	
	@Override
	public void fetchDownloadCounts(AddOnToIndex toIndex, AddOnInfoAndVersions infoAndVersions) {
		try {
			NpmDownloadCount count = restTemplate.getForObject(DOWNLOADS_URL, NpmDownloadCount.class,
			    Map.of("package", toIndex.getNpmPackage()));
			if (count != null && count.getDownloads() != null) {
				infoAndVersions.setDownloadCountInLast30Days(count.getDownloads());
			} else {
				log.warn("No download count in the response for {}", toIndex.getNpmPackage());
			}
		}
		catch (Exception ex) {
			// a missing download count must never cost us the versions we already collected
			log.warn("Could not fetch download counts for {}", toIndex.getNpmPackage(), ex);
		}
	}
	
	@Override
	public void fetchVersionDetails(AddOnToIndex toIndex, AddOnVersion addOnVersion) throws IOException {
		String npmPackage = toIndex.getNpmPackage();
		log.info("Fetching routes.json for {} {}", toIndex.getUid(), addOnVersion.getVersion());
		String routesJson = fetchRoutesJson(npmPackage, addOnVersion.getVersion().toString());
		if (routesJson == null) {
			// a frontend module without a routes.json declares no backend dependencies; index it anyway
			log.warn("No dist/routes.json for {}@{}", npmPackage, addOnVersion.getVersion());
			return;
		}
		handleRoutesJson(routesJson, addOnVersion);
	}
	
	/**
	 * Fetches one version's routes.json from unpkg. Returns null if the package published no
	 * routes.json for this version. Throws if unpkg cannot serve the version at all, which happens for
	 * a short window after a publish: indexing "declares no dependencies" in that window would be wrong
	 * data that the reuse check then keeps forever.
	 */
	private String fetchRoutesJson(String npmPackage, String version) {
		try {
			return restTemplate.getForObject(UNPKG_ROUTES_URL, String.class,
			    Map.of("package", npmPackage, "version", version));
		}
		catch (HttpClientErrorException.NotFound ex) {
			// a 404 is ambiguous: no routes.json in the tarball, or unpkg lagging behind the
			// registry. Every published version has a package.json, so it tells the two apart.
			try {
				restTemplate.getForObject(UNPKG_PACKAGE_JSON_URL, String.class,
				    Map.of("package", npmPackage, "version", version));
			}
			catch (HttpClientErrorException.NotFound versionUnknown) {
				throw new IllegalStateException(
				        npmPackage + "@" + version + " is not on unpkg yet; a later run will retry it", versionUnknown);
			}
			return null;
		}
	}
	
	/**
	 * Reads the two backend dependency maps out of a frontend module's routes.json. Frontend modules
	 * depend on backend modules rather than on an OpenMRS core version, so requireOpenmrsVersion is
	 * deliberately left unset.
	 */
	void handleRoutesJson(String routesJson, AddOnVersion addOnVersion) throws IOException {
		JsonNode root = objectMapper.readTree(routesJson);
		// anything unexpected throws rather than quietly indexing "declares no dependencies":
		// the caller drops the version for this run and a later run retries it
		if (!root.isObject()) {
			throw new IOException("Expected routes.json to be an object but got " + root.getNodeType());
		}
		addBackendDependencies(root.path("backendDependencies"), addOnVersion, false);
		addBackendDependencies(root.path("optionalBackendDependencies"), addOnVersion, true);
	}
	
	private void addBackendDependencies(JsonNode dependencies, AddOnVersion addOnVersion, boolean optional)
	        throws IOException {
		if (dependencies.isMissingNode() || dependencies.isNull()) {
			// legitimately absent: many apps declare no optional (or no required) dependencies.
			// An explicit null is schema-invalid but harmless, so it must not fail the version.
			return;
		}
		String mapName = optional ? "optionalBackendDependencies" : "backendDependencies";
		if (!dependencies.isObject()) {
			throw new IOException("Expected an object for " + mapName + " but got " + dependencies.getNodeType());
		}
		for (Map.Entry<String, JsonNode> entry : dependencies.properties()) {
			JsonNode value = entry.getValue();
			// a dependency is either a bare version string, or an object whose "version" we want and
			// whose "feature" block describes a UI feature flag we don't index
			JsonNode version = value.isObject() ? value.path("version") : value;
			if (!version.isTextual()) {
				throw new IOException("No usable version for " + entry.getKey() + " in " + mapName);
			}
			// routes.json expresses versions as SemVer ranges, but the rest of the index uses strict
			// minimums and wildcards, so they are translated here. As in content.properties, a
			// requirement whose range we can't translate is still worth recording without one.
			String requiredVersion = VersionRangeConverter.toOpenmrsVersionRange(version.asText());
			if (requiredVersion == null) {
				log.warn("Cannot express version range \"{}\" of {} as an OpenMRS version range", version.asText(),
				    entry.getKey());
			}
			addOnVersion.addRequiredModule(BACKEND_MODULE_PREFIX + entry.getKey(), requiredVersion, optional);
		}
	}
	
	private AddOnVersion toAddOnVersion(PublishedVersion publishedVersion) {
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version(publishedVersion.version()));
		version.setDownloadUri(publishedVersion.tarball());
		// renameTo is deliberately left null: the tarball is already named esm-billing-app-1.3.1.tgz,
		// so the UI links straight to npm instead of proxying through RenamingFileProxyController.
		
		version.setReleaseDatetime(publishedVersion.published());
		
		return version;
	}
}
