package org.openmrs.addonindex.backend;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.artifactory.AqlArtifactInfo;
import org.openmrs.addonindex.domain.artifactory.AqlSearchResponse;
import org.openmrs.addonindex.domain.artifactory.ArtifactoryArtifactDetails;
import org.openmrs.addonindex.domain.artifactory.GavcSearchResponse;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * <p>A {@link BackendHandler} for OpenMRS's Artifactory instance using the Artifactory API to find AddOns.</p>
 * <p>We use two different searching mechanisms depending on whether or not an API key for Artifactory is available. It's
 * always preferable to have the API key available, as this results in fewer API requests, but an fallback mechanism
 * is provided using Artifactory's public API.</p>
 */
@Component
@Slf4j
public class Artifactory implements BackendHandler {

	public static final String ARTIFACTORY_URL = "https://openmrs.jfrog.io/artifactory";

	protected static final String AQL_URL = ARTIFACTORY_URL + "/api/search/aql";

	private static final String AQL_SEARCH_TEMPLATE_JAR = "items.find({"
			+ "\"$and\": ["
			+ "{\"repo\": {\"$nmatch\": \"*snapshots\"}},"
			+ "{\"path\": {\"$match\": \"%1$s/%2$s/*\"}},"
			+ "{\"name\": {\"$match\" : \"%2$s*.jar\"}}"
			+ "] }).include(\"name\", \"repo\", \"path\", \"created\", \"stat.downloads\")";

	private static final String AQL_SEARCH_TEMPLATE_OMOD = "items.find({"
			+ "\"$and\": ["
			+ "{\"repo\": {\"$match\": \"omod\"}},"
			+ "{\"path\": {\"$match\": \"%1$s/%2$s/*\"}},"
			+ "{\"name\": {\"$match\" : \"%2$s*.omod\"}}"
			+ "] }).include(\"name\", \"repo\", \"path\", \"created\", \"stat.downloads\")";

	protected static final String GAVC_URL = ARTIFACTORY_URL + "/api/search/gavc?g={g}&a={a}&repos={repos}";

	private static final Pattern OMOD_RELEASED_VERSION_JAR = Pattern
			.compile("(?<name>.+)-(?i:omod)-(?<version>[0-9.]+)\\.(?i:(?<extension>jar|omod))");

	private static final Pattern OMOD_RELEASED_VERSION_OMOD = Pattern
			.compile("(?<name>.+)-(?<version>[0-9.]+)\\.(?i:(?<extension>omod))");

	private static final Pattern OWA_RELEASED_VERSION = Pattern
			.compile("(?<name>.+)-(?<version>[0-9.]+)\\.(?i:(?<extensiont>zip))");

	private enum OMOD_EXT {
		OMOD,
		JAR
	}

	private final RestTemplate restTemplate;

	private final String artifactoryApiKey;

	@Autowired
	public Artifactory(RestTemplate restTemplate, @Value("${artifactory.api_key}") String artifactoryApiKey) {
		this.restTemplate = restTemplate;
		this.artifactoryApiKey = artifactoryApiKey;
	}

	@Override
	public AddOnInfoAndVersions getInfoAndVersionsFor(AddOnToIndex addOnToIndex) throws Exception {
		if (addOnToIndex.getMavenRepoDetails() == null) {
			throw new IllegalStateException("No maven repo details provided for AddOn: " + addOnToIndex.getName());
		}

		AddOnInfoAndVersions result;
		if (artifactoryApiKey != null && !artifactoryApiKey.isEmpty()) {
			result = getInfoAndVersionsAql(addOnToIndex);
		} else {
			result = getInfoAndVersionsGavc(addOnToIndex);
		}

		Collections.sort(result.getVersions());

		return result;
	}

	private AddOnInfoAndVersions getInfoAndVersionsAql(AddOnToIndex addOnToIndex) {
		AddOnInfoAndVersions result = AddOnInfoAndVersions.from(addOnToIndex);

		String groupPath = addOnToIndex.getMavenRepoDetails().getGroupId().replace(".", "/").toLowerCase(Locale.ROOT);
		String artifact = addOnToIndex.getMavenRepoDetails().getArtifactId().toLowerCase(Locale.ROOT);

		SortedSet<AddOnVersion> versions = new TreeSet<>();

		if (addOnToIndex.getType() == AddOnType.OMOD) {
			if (!artifact.endsWith("-omod")) {
				versions.addAll(runAqlQueryFor(addOnToIndex, groupPath, artifact + "-omod"));
			}
		}

		versions.addAll(runAqlQueryFor(addOnToIndex, groupPath, artifact));

		result.getVersions().addAll(versions);

		return result;
	}

	private List<AddOnVersion> runAqlQueryFor(AddOnToIndex addOnToIndex, String groupPath, String artifact) {
		final List<AddOnVersion> result = new ArrayList<>();
		final String AQL_SEARCH_TEMPLATE = artifact.endsWith("-omod") ? AQL_SEARCH_TEMPLATE_JAR : AQL_SEARCH_TEMPLATE_OMOD;
		final String requestBody = String.format(AQL_SEARCH_TEMPLATE, groupPath, artifact);

		ResponseEntity<AqlSearchResponse> responseEntity = restTemplate.execute(
				AQL_URL, HttpMethod.POST, request -> {
					byte[] content = requestBody.getBytes(StandardCharsets.UTF_8);

					HttpHeaders headers = request.getHeaders();
					headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + artifactoryApiKey);
					headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");
					headers.add(HttpHeaders.CONTENT_LENGTH, "" + content.length);

					request.getBody().write(content);
				}, restTemplate.responseEntityExtractor(AqlSearchResponse.class));

		if (responseEntity == null) {
			log.error("Did not receive a result for {}:{} from AQL API",
					addOnToIndex.getMavenRepoDetails().getGroupId(),
					addOnToIndex.getMavenRepoDetails().getArtifactId());

			return Collections.emptyList();
		}

		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.warn("Problem fetching {} -> {} {}", AQL_URL, responseEntity.getStatusCode(), responseEntity.getBody());
			return Collections.emptyList();
		}

		AqlSearchResponse searchResponse = responseEntity.getBody();

		if (searchResponse == null) {
			log.warn("No response returned for {}:{}",
					addOnToIndex.getMavenRepoDetails().getGroupId(),
					addOnToIndex.getMavenRepoDetails().getArtifactId());

			return Collections.emptyList();
		}

		for (AqlArtifactInfo info : searchResponse.getResults()) {
			Matcher m = getMatcherFor(addOnToIndex, info.getName(),
					artifact.endsWith("-omod") ? OMOD_EXT.JAR : OMOD_EXT.OMOD);
			if (m.matches()) {
				AddOnVersion version = new AddOnVersion();
				version.setVersion(new Version(m.group("version")));
				version.setReleaseDatetime(OffsetDateTime.parse(info.getCreated()));
				version.setDownloadUri(String.join("/", ARTIFACTORY_URL, info.getRepo(), info.getPath(), info.getName()));
				version.setRenameTo(
						m.group("name") + "-" + m.group("version") + "." + addOnToIndex.getType().getFileExtension());
				result.add(version);
			}
		}

		return result;
	}

	private AddOnInfoAndVersions getInfoAndVersionsGavc(AddOnToIndex addOnToIndex) {
		AddOnInfoAndVersions result = AddOnInfoAndVersions.from(addOnToIndex);

		String group = addOnToIndex.getMavenRepoDetails().getGroupId().toLowerCase(Locale.ROOT);
		String artifact = addOnToIndex.getMavenRepoDetails().getArtifactId().toLowerCase(Locale.ROOT);

		SortedSet<AddOnVersion> versions = new TreeSet<>();

		if (addOnToIndex.getType() == AddOnType.OMOD) {
			if (!artifact.endsWith("-omod")) {
				versions.addAll(runGavcQueryFor(addOnToIndex, group, artifact + "-omod"));
			}
		}

		versions.addAll(runGavcQueryFor(addOnToIndex, group, artifact));

		result.getVersions().addAll(versions);

		return result;
	}

	private List<AddOnVersion> runGavcQueryFor(AddOnToIndex addOnToIndex, String group, String artifact) {
		String repos = "modules,owa";
		if (addOnToIndex.getType() == AddOnType.OMOD && !artifact.endsWith("-omod")) {
			repos = "omod";
		}

		ResponseEntity<GavcSearchResponse> responseEntity = restTemplate
				.getForEntity(GAVC_URL, GavcSearchResponse.class, Map.of(
						"g", group,
						"a", artifact,
						"repos", repos
				));

		if (!responseEntity.getStatusCode().is2xxSuccessful()) {
			log.warn("Problem fetching {} -> {} {}", AQL_URL, responseEntity.getStatusCode(), responseEntity.getBody());
			return Collections.emptyList();
		}

		if (responseEntity.getBody() == null) {
			log.error("Did not receive a result for {}:{} from GAVC API",
					addOnToIndex.getMavenRepoDetails().getGroupId(),
					addOnToIndex.getMavenRepoDetails().getArtifactId());

			return Collections.emptyList();
		}

		List<AddOnVersion> result = new ArrayList<>();
		for (GavcSearchResponse.GavcUri uri : responseEntity.getBody().getResults()) {
			if (uri == null || uri.getUri() == null || uri.getUri().isEmpty()) {
				continue;
			}

			String[] components = uri.getUri().split("/");
			Matcher m = getMatcherFor(addOnToIndex, components[components.length - 1],
					repos.equals("omod") ? OMOD_EXT.OMOD : OMOD_EXT.JAR);
			if (m.matches()) {
				ArtifactoryArtifactDetails details = restTemplate
						.getForObject(uri.getUri(), ArtifactoryArtifactDetails.class);

				if (details == null) {
					log.warn("Could not fetch details for {}", uri.getUri());
					continue;
				}

				AddOnVersion version = new AddOnVersion();
				version.setVersion(new Version(m.group("version")));
				version.setReleaseDatetime(OffsetDateTime.parse(details.getCreated()));
				version.setDownloadUri(details.getDownloadUri());
				version.setRenameTo(
						m.group("name") + "-" + m.group("version") + "." + addOnToIndex.getType().getFileExtension());
				result.add(version);
			}
		}

		return result;
	}

	private Matcher getMatcherFor(AddOnToIndex addOn, String stringToMatch, OMOD_EXT extension) {
		if (addOn.getType() == AddOnType.OMOD) {
			if (extension == OMOD_EXT.OMOD) {
				return OMOD_RELEASED_VERSION_OMOD.matcher(stringToMatch);
			} else {
				return OMOD_RELEASED_VERSION_JAR.matcher(stringToMatch);
			}
		} else {
			return OWA_RELEASED_VERSION.matcher(stringToMatch);
		}
	}
}
