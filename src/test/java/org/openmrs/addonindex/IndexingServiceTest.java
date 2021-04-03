package org.openmrs.addonindex;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.backend.Artifactory;
import org.openmrs.addonindex.backend.Bintray;
import org.openmrs.addonindex.backend.Modulus;
import org.openmrs.addonindex.backend.OpenmrsMavenRepo;
import org.openmrs.addonindex.domain.AddOnList;
import org.openmrs.addonindex.domain.AddOnReference;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.Link;
import org.openmrs.addonindex.domain.Maintainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

/**
 * The idea is that the add-ons-to-index.json file represents all the modules we want to index, and the application will
 * periodically refresh it from github. Further, we expect module authors to submit pull requests modifying that file
 * in order to add their own modules for indexing. This class will test for various scenarios to ensure the file remains
 * well-formed, consistent, and doesn't violate any constraints. (CI should report if any PRs violate these tests.)
 */
@JsonTest
public class IndexingServiceTest {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private AllAddOnsToIndex toIndex;
	
	@BeforeEach
	public void setUp() throws Exception {
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("add-ons-to-index.json");
		toIndex = objectMapper.readValue(resourceStream, AllAddOnsToIndex.class);
	}
	
	@Test
	public void testParseable() {
		assertThat(toIndex.size(), not(0));
		assertThat(toIndex.getToIndex().get(0).getName(), notNullValue());
	}
	
	@Test
	public void testUidPresentAndUnique() {
		Set<String> already = new HashSet<>();
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			assertThat("UID is required", addOn.getUid(), notNullValue());
			assertThat("UID is not unique:" + addOn.getUid(), already, not(hasItem(addOn.getUid())));
			already.add(addOn.getUid());
		}
	}
	
	@Test
	public void testNamesAndDescriptionPresentAndNotTooLong() {
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			assertThat(addOn.getName(), notNullValue());
			assertThat(addOn.getName().length(), lessThan(100));
			if (addOn.getBackend().equals(OpenmrsMavenRepo.class)) {
				assertThat(addOn.getDescription(), notNullValue());
				assertThat(addOn.getDescription().length(), lessThan(1000));
			}
		}
	}
	
	@Test
	public void testMaintainersPresentAndNamesNotTooLong() {
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			assertThat(addOn.getMaintainers(), notNullValue());
			assertThat("maintainers for " + addOn.getUid(), addOn.getMaintainers().size(), greaterThan(0));
			for (Maintainer maintainer : addOn.getMaintainers()) {
				assertThat(maintainer.getName(), notNullValue());
				assertThat(maintainer.getName().length(), lessThan(100));
			}
		}
	}
	
	@Test
	public void testLinksHaveRelAndHrefAndOptionalTitle() {
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			if (addOn.getLinks() != null) {
				for (Link link : addOn.getLinks()) {
					assertThat(link.getRel(), notNullValue());
					assertThat(link.getRel().length(), lessThan(100));
					assertThat(link.getHref(), notNullValue());
					assertThat(link.getHref().length(), lessThan(300));
					if (link.getTitle() != null) {
						assertThat(link.getTitle().length(), lessThan(200));
					}
				}
			}
		}
	}
	
	@Test
	public void testModuleBackendDetails() {
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			assertThat(addOn.getBackend(), notNullValue());
		}

		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			if (addOn.getBackend().equals(Modulus.class)) {
				assertThat(addOn.getModulusDetails().getId(), notNullValue());
			} else if (addOn.getBackend().equals(Bintray.class)) {
				assertThat(addOn.getBintrayPackageDetails().getOwner(), notNullValue());
				assertThat(addOn.getBintrayPackageDetails().getRepo(), notNullValue());
				assertThat(addOn.getBintrayPackageDetails().getPackageName(), notNullValue());
			} else if (addOn.getBackend().equals(Artifactory.class)) {
				assertThat(addOn.getMavenRepoDetails().getGroupId(), notNullValue());
				assertThat(addOn.getMavenRepoDetails().getArtifactId(), notNullValue());
			} else if (addOn.getBackend().equals(OpenmrsMavenRepo.class)) {
				assertThat(addOn.getMavenRepoDetails().getGroupId(), notNullValue());
				assertThat(addOn.getMavenRepoDetails().getArtifactId(), notNullValue());
			} else {
				fail("Unrecognized backend: " + addOn.getBackend());
			}
		}
	}
	
	@Test
	public void testListsHaveNames() {
		for (AddOnList list : toIndex.getLists()) {
			assertThat(list.getName(), notNullValue());
			assertThat(list.getAddOns(), hasSize(greaterThan(0)));
		}
	}
	
	@Test
	public void testListsReferToAddOnsThatWeAreIndexing() {
		for (AddOnList list : toIndex.getLists()) {
			for (AddOnReference reference : list.getAddOns()) {
				assertThat(reference.getUid() + " does not refer to an indexed add-on",
						toIndex.getAddOnByUid(reference.getUid()), optionalWithValue());
			}
		}
	}
	
	@Test
	public void testTagsHaveNoWhitespace() {
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			if (addOn.getTags() != null) {
				for (String tag : addOn.getTags()) {
					assertThat(tag, not(emptyOrNullString()));
					assertThat("Tag should not have whitespace: \"" + tag + "\" (" + addOn.getUid() + ")",
							tag, matchesPattern("[^\\s]*"));
				}
			}
		}
		
	}
}
