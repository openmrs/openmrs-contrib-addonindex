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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.backend.NpmPackageDetails;
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
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link BackendHandler} for OpenMRS frontend modules published to the npmjs.com registry.
 */
@Component
@Slf4j
public class NpmJs implements BackendHandler, SupportsDownloadCounts, SupportsVersionDetails {
	
	protected static final String REGISTRY_URL = "https://registry.npmjs.org/{package}";
	
	protected static final String DOWNLOADS_URL = "https://api.npmjs.org/downloads/point/last-month/{package}";
	
	/**
	 * The path of the routes file inside a version's tarball, below the single leading directory
	 * (conventionally "package/") that npm pack puts everything under.
	 */
	private static final String ROUTES_JSON_TARBALL_PATH = "dist/routes.json";
	
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
	 * If-None-Match lets the registry answer 304 with an empty body whenever nothing has been published
	 * since, which skips both the download and the parse.
	 */
	private final Map<String, CachedPackument> cachedPackuments = new ConcurrentHashMap<>();
	
	@Autowired
	public NpmJs(RestTemplate restTemplate, ObjectMapper objectMapper) {
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
			if (versions.isEmpty()) {
				// fetchPackument throws first with a message naming the package; this guards any
				// future construction site, because a cached empty list would replay on every 304
				throw new IllegalStateException("must never cache an empty version list");
			}
			// the cache must never hand out a list a caller could mutate
			versions = List.copyOf(versions);
		}
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		String npmPackage = packageNameFor(addOnToIndex);
		
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
	
	private String packageNameFor(AddOnToIndex addOnToIndex) {
		NpmPackageDetails details = addOnToIndex.getNpmPackageDetails();
		if (details == null || !StringUtils.hasText(details.getPackageName())) {
			throw new IllegalStateException("No npm package details for AddOn: " + addOnToIndex.getName());
		}
		return details.getPackageName();
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
		if (packument.getTime() == null) {
			// unlike a single bad timestamp (warned per version below), a missing map would
			// otherwise silently cost every version its release date
			log.warn("Packument for {} has no time map; versions will index without release dates", npmPackage);
		}
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
		String npmPackage = packageNameFor(toIndex);
		try {
			NpmDownloadCount count = restTemplate.getForObject(DOWNLOADS_URL, NpmDownloadCount.class,
			    Map.of("package", npmPackage));
			if (count != null && count.getDownloads() != null) {
				infoAndVersions.setDownloadCountInLast30Days(count.getDownloads());
			} else {
				log.warn("No download count in the response for {}", npmPackage);
			}
		}
		catch (Exception ex) {
			// a missing download count must never cost us the versions we already collected
			log.warn("Could not fetch download counts for {}", npmPackage, ex);
		}
	}
	
	@Override
	public void fetchVersionDetails(AddOnToIndex toIndex, AddOnVersion addOnVersion) throws IOException {
		log.info("Fetching tarball of {} {} for its routes.json", toIndex.getUid(), addOnVersion.getVersion());
		String routesJson = fetchRoutesJson(addOnVersion);
		if (routesJson == null) {
			// a frontend module without a routes.json declares no backend dependencies; index it anyway
			log.warn("No {} in {}", ROUTES_JSON_TARBALL_PATH, addOnVersion.getDownloadUri());
			return;
		}
		handleRoutesJson(routesJson, addOnVersion);
	}
	
	/**
	 * Reads one version's routes.json out of its tarball on the registry, the same way OMOD and content
	 * package details are read out of their zips. Returns null if the version ships no routes.json. A
	 * failure to fetch or read the tarball propagates: the caller then drops the version for this run,
	 * and a later run retries it.
	 * <p>
	 * The multi-MB tarball is streamed rather than buffered, which caps memory use, not bandwidth:
	 * npm's entry ordering usually puts dist/routes.json late in the archive, so most of the transfer
	 * still happens, and returning from the extractor abandons only the remainder.
	 */
	private String fetchRoutesJson(AddOnVersion addOnVersion) {
		return restTemplate.execute(addOnVersion.getDownloadUri(), HttpMethod.GET, null, response -> {
			try (TarArchiveInputStream tar = new TarArchiveInputStream(
			        new GzipCompressorInputStream(new BufferedInputStream(response.getBody())))) {
				TarArchiveEntry entry;
				while ((entry = tar.getNextEntry()) != null) {
					String name = entry.getName();
					int slash = name.indexOf('/');
					if (slash >= 0 && name.substring(slash + 1).equals(ROUTES_JSON_TARBALL_PATH)) {
						return StreamUtils.copyToString(tar, StandardCharsets.UTF_8);
					}
				}
			}
			return null;
		});
	}
	
	/**
	 * Reads the backend dependency map out of a frontend module's routes.json. The
	 * optionalBackendDependencies map is deliberately not indexed, for parity with how config.xml
	 * indexing considers require_module directives but not aware_of ones. Frontend modules depend on
	 * backend modules rather than on an OpenMRS core version, so requireOpenmrsVersion is deliberately
	 * left unset.
	 */
	void handleRoutesJson(String routesJson, AddOnVersion addOnVersion) throws IOException {
		JsonNode root = objectMapper.readTree(routesJson);
		// anything unexpected throws rather than quietly indexing "declares no dependencies":
		// the caller drops the version for this run and a later run retries it
		if (!root.isObject()) {
			throw new IOException("Expected routes.json to be an object but got " + root.getNodeType());
		}
		JsonNode dependencies = root.path("backendDependencies");
		if (dependencies.isMissingNode() || dependencies.isNull()) {
			// legitimately absent: many apps declare no backend dependencies.
			// An explicit null is schema-invalid but harmless, so it must not fail the version.
			return;
		}
		if (!dependencies.isObject()) {
			throw new IOException("Expected an object for backendDependencies but got " + dependencies.getNodeType());
		}
		for (Map.Entry<String, JsonNode> entry : dependencies.properties()) {
			JsonNode value = entry.getValue();
			// a dependency is either a bare version string, or an object whose "version" we want and
			// whose "feature" block describes a UI feature flag we don't index
			JsonNode version = value.isObject() ? value.path("version") : value;
			if (!version.isTextual()) {
				throw new IOException("No usable version for " + entry.getKey() + " in backendDependencies");
			}
			// routes.json expresses versions as SemVer ranges, but the rest of the index uses strict
			// minimums and wildcards, so they are translated here. As in content.properties, a
			// requirement whose range we can't translate is still worth recording without one.
			String requiredVersion = VersionRangeConverter.toOpenmrsVersionRange(version.asText());
			if (requiredVersion == null) {
				log.warn("Cannot express version range \"{}\" of {} as an OpenMRS version range", version.asText(),
				    entry.getKey());
			}
			addOnVersion.addRequiredModule(BACKEND_MODULE_PREFIX + entry.getKey(), requiredVersion);
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
