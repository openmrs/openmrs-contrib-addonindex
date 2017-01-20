package org.openmrs.addonindex.backend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.springframework.util.StreamUtils;

public class OpenmrsMavenRepoTest {
	
	@Test
	public void testMaven() throws Exception {
		String xml = StreamUtils.copyToString(
				getClass().getClassLoader().getResourceAsStream("indexBrowserTreeViewResponse.xml"),
				Charset.defaultCharset());
		
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("appui-module");
		toIndex.setName("App UI Module");
		toIndex.setType(AddOnType.OMOD);
		
		OpenmrsMavenRepo backend = new OpenmrsMavenRepo();
		AddOnInfoAndVersions addOnInfoAndVersions = backend.handleIndexBrowserTreeViewResponse(toIndex, xml);
		
		assertThat(addOnInfoAndVersions.getUid(), is(toIndex.getUid()));
		assertThat(addOnInfoAndVersions.getName(), is(toIndex.getName()));
		assertThat(addOnInfoAndVersions.getType(), is(toIndex.getType()));
		
		assertThat(addOnInfoAndVersions.getVersions().size(), is(10));
		
		assertThat(addOnInfoAndVersions.getVersions().first().getVersion().toString(), is("1.0"));
		assertThat(addOnInfoAndVersions.getVersions().first().getDownloadUri(),
				is("http://mavenrepo.openmrs.org/nexus/service/local/repositories/modules/content/org/openmrs/module/appui"
						+ "-omod/1.0/appui-omod-1.0.jar"));
		assertThat(addOnInfoAndVersions.getVersions().first().getRenameTo(), is("appui-1.0.omod"));
		
		assertThat(addOnInfoAndVersions.getVersions().last().getVersion().toString(), is("1.7"));
		assertThat(addOnInfoAndVersions.getVersions().last().getDownloadUri(),
				is("http://mavenrepo.openmrs.org/nexus/service/local/repositories/modules/content/org/openmrs/module/appui"
						+ "-omod/1.7/appui-omod-1.7.jar"));
		assertThat(addOnInfoAndVersions.getVersions().last().getRenameTo(), is("appui-1.7.omod"));
	}
	
}