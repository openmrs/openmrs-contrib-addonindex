package org.openmrs.addonindex.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@Disabled
@SpringBootTest
public class ArtifactoryManualTest {

	@Autowired
	private RestTemplate restTemplate;

	@Test
	public void testFetchingArtifact() throws Exception {
		Artifactory artifactory = new Artifactory(restTemplate, "");

		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("fhir2-module");
		toIndex.setName("FHIR2 Module");
		toIndex.setType(AddOnType.OMOD);
		toIndex.setMavenRepoDetails(new MavenRepoDetails("org.openmrs.module", "fhir2"));

		AddOnInfoAndVersions versionsFor = artifactory.getInfoAndVersionsFor(toIndex);

		System.out.println(versionsFor.getName());
		System.out.println(versionsFor.getVersions().get(0).getDownloadUri());
		assertThat(versionsFor.getName(), is("FHIR2 Module"));
		assertThat(versionsFor.getVersions(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(versionsFor.getVersions(),
				hasItem(hasProperty("version", hasToString("1.0.0"))));
	}

	@Test
	public void testFetchingArtifactWithApiKey() throws Exception {
		Artifactory artifactory = new Artifactory(restTemplate, "INSERT VALID KEY HERE");

		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("fhir2-module");
		toIndex.setName("FHIR2 Module");
		toIndex.setType(AddOnType.OMOD);
		toIndex.setMavenRepoDetails(new MavenRepoDetails("org.openmrs.module", "fhir2"));

		AddOnInfoAndVersions versionsFor = artifactory.getInfoAndVersionsFor(toIndex);

		System.out.println(versionsFor.getName());
		System.out.println(versionsFor.getVersions().get(0).getDownloadUri());
		assertThat(versionsFor.getName(), is("FHIR2 Module"));
		assertThat(versionsFor.getVersions(), hasSize(greaterThanOrEqualTo(1)));
		assertThat(versionsFor.getVersions(),
				hasItem(hasProperty("version", hasToString("1.0.0"))));
	}
}
