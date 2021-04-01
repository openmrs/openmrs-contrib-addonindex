package org.openmrs.addonindex.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.backend.MavenRepoDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test makes an HTTP call to the OpenMRS maven repo. Enable this if you want to do manual troubleshooting.
 */
@Disabled
@SpringBootTest
public class OpenmrsMavenRepoManualTest {
	
	@Autowired
	private OpenmrsMavenRepo backend;
	
	@Test
	public void manualTestMakingWebCall() throws Exception {
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("appui-module");
		toIndex.setName("App UI Module");
		toIndex.setType(AddOnType.OMOD);
		toIndex.setMavenRepoDetails(new MavenRepoDetails("org.openmrs.module", "appui-omod"));
		
		AddOnInfoAndVersions versionsFor = backend.getInfoAndVersionsFor(toIndex);
		System.out.println(versionsFor.getName());
		System.out.println(versionsFor.getVersions().get(0).getDownloadUri());
		assertThat(versionsFor.getName(), is("App UI Module"));
		assertThat(versionsFor.getVersions().get(0).getVersion().toString(), is("1.0"));
		assertThat(versionsFor.getVersions().get(0).getRequireOpenmrsVersion(), is("1.9.1"));
	}
	
}
