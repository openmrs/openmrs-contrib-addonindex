package org.openmrs.addonindex.backend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * This test makes an HTTP call to the OpenMRS maven repo. Unignore this if you want to do manual troubleshooting.
 */
@Ignore
@RunWith(SpringRunner.class)
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
		System.out.println(versionsFor.getVersions().first().getDownloadUri());
		assertThat(versionsFor.getName(), is("App UI Module"));
		assertThat(versionsFor.getVersions().last().getVersion().toString(), is("1.0"));
		assertThat(versionsFor.getVersions().last().getRequireOpenmrsVersion(), is("1.9.1"));
	}
	
}