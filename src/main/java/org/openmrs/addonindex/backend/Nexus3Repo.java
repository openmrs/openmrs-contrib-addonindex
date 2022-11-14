package org.openmrs.addonindex.backend;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.nexus3.Nexus3SearchResponse;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class Nexus3Repo implements BackendHandler {
	
	private static final String NEXUS3_SEARCH_TEMPLATE = "service/rest/v1/search/assets?"
			+ "maven.groupId={group}&"
			+ "maven.artifactId={artifact}&sort=version";
	
	private final RestTemplate restTemplate;
	
	private static final Pattern OMOD_RELEASED_VERSION_JAR = Pattern
			.compile(".*/(?<name>.+)-(?i:omod)-(?<version>[0-9.]+)\\.(?i:jar)");
	
	@Autowired
	public Nexus3Repo(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		if (addOnToIndex.getMavenRepoDetails() == null) {
			throw new IllegalStateException(
					"Must supply Maven repository details for the addon " + addOnToIndex.getUid());
		}
		
		if (addOnToIndex.getMavenRepoDetails().getGroupId() == null || addOnToIndex.getMavenRepoDetails().getGroupId()
				.isEmpty()) {
			throw new IllegalStateException(
					"Must supply group id for the addon " + addOnToIndex.getUid());
		}
		
		if (addOnToIndex.getMavenRepoDetails().getArtifactId() == null || addOnToIndex.getMavenRepoDetails().getArtifactId()
				.isEmpty()) {
			throw new IllegalStateException(
					"Must supply artifact id for the addon " + addOnToIndex.getUid());
		}
		
		AddOnInfoAndVersions result = AddOnInfoAndVersions.from(addOnToIndex);
		
		String finalUrl = getBaseUrlFor(addOnToIndex) + NEXUS3_SEARCH_TEMPLATE;
		
		String artifactId = addOnToIndex.getMavenRepoDetails().getArtifactId();
		if (!artifactId.endsWith("-omod")) {
			artifactId += "-omod";
		}
		
		ResponseEntity<Nexus3SearchResponse> responseEntity = restTemplate
				.getForEntity(finalUrl, Nexus3SearchResponse.class, Map.of(
						"group", addOnToIndex.getMavenRepoDetails().getGroupId(),
						"artifact", artifactId
				));
		
		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.warn("Problem fetching {} -> {} {}", finalUrl, responseEntity.getStatusCode(), responseEntity.getBody());
			return result;
		}
		
		if (responseEntity.getBody() == null) {
			log.error("Did not receive a result for {}:{} from Nexus API at {}",
					addOnToIndex.getMavenRepoDetails().getGroupId(),
					addOnToIndex.getMavenRepoDetails().getArtifactId(),
					addOnToIndex.getMavenRepoDetails().getRepoUrl());
			
			return result;
		}
		
		SortedSet<AddOnVersion> versions = new TreeSet<>();
		
		Nexus3SearchResponse searchResponse = responseEntity.getBody();
		
		if (searchResponse == null) {
			log.warn("No response returned for {}:{} from Nexus API at {}",
					addOnToIndex.getMavenRepoDetails().getGroupId(),
					addOnToIndex.getMavenRepoDetails().getArtifactId(),
					addOnToIndex.getMavenRepoDetails().getRepoUrl());
			
			return result;
		}
		
		List<Nexus3SearchResponse.Nexus3SearchItemDetails> items = searchResponse.getItems();
		
		if (items == null || items.isEmpty()) {
			log.info("No items returned for {}:{} from Nexus API at {}",
					addOnToIndex.getMavenRepoDetails().getGroupId(),
					addOnToIndex.getMavenRepoDetails().getArtifactId(),
					addOnToIndex.getMavenRepoDetails().getRepoUrl());
			
			return result;
		}
		
		for (Nexus3SearchResponse.Nexus3SearchItemDetails details : items) {
			Matcher m = OMOD_RELEASED_VERSION_JAR.matcher(details.getPath());
			if (m.matches()) {
				AddOnVersion version = new AddOnVersion();
				version.setVersion(new Version(m.group("version")));
				version.setDownloadUri(details.getDownloadUrl());
				version.setRenameTo(
						m.group("name") + "-" + m.group("version") + "." + addOnToIndex.getType().getFileExtension());
				versions.add(version);
			}
		}
		
		result.getVersions().addAll(versions);
		
		return result;
	}
	
	private String getBaseUrlFor(AddOnToIndex addOnToIndex) {
		if (addOnToIndex.getMavenRepoDetails() == null) {
			throw new IllegalStateException(
					"Must supply Maven repository details for the addon " + addOnToIndex.getUid());
		}
		
		String baseUrl = addOnToIndex.getMavenRepoDetails().getRepoUrl();
		if (baseUrl == null || baseUrl.isEmpty()) {
			throw new IllegalStateException(
					"Must supply a URL for Nexus Maven repository to use for " + addOnToIndex.getUid());
		}
		
		try {
			new URL(baseUrl);
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException(baseUrl + " is not a valid URL for the repository for " + addOnToIndex.getUid(),
					e);
		}
		
		if (!baseUrl.endsWith("/")) {
			return baseUrl + "/";
		}
		
		return baseUrl;
	}
}
