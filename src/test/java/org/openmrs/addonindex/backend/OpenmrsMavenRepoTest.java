package org.openmrs.addonindex.backend;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.springframework.util.StreamUtils;

public class OpenmrsMavenRepoTest {
	
	@Test
	public void testHandlingNexusXml() throws Exception {
		String xml = StreamUtils.copyToString(
				getClass().getClassLoader().getResourceAsStream("indexBrowserTreeViewResponse.xml"),
				Charset.defaultCharset());
		
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setUid("appui-module");
		toIndex.setName("App UI Module");
		toIndex.setDescription("For apps");
		toIndex.setType(AddOnType.OMOD);
		
		OpenmrsMavenRepo backend = new OpenmrsMavenRepo() {
			
			@Override
			String fetchConfigXml(AddOnToIndex addOnToIndex, AddOnVersion addOnVersion) throws IOException {
				return null;
			}
		};
		AddOnInfoAndVersions addOnInfoAndVersions = backend.handleIndexBrowserTreeViewResponse(toIndex, xml);
		
		assertThat(addOnInfoAndVersions.getUid(), is(toIndex.getUid()));
		assertThat(addOnInfoAndVersions.getName(), is(toIndex.getName()));
		assertThat(addOnInfoAndVersions.getDescription(), is(toIndex.getDescription()));
		assertThat(addOnInfoAndVersions.getType(), is(toIndex.getType()));
		
		assertThat(addOnInfoAndVersions.getVersions().size(), is(10));
		
		assertThat(addOnInfoAndVersions.getVersions().last().getVersion().toString(), is("1.0"));
		assertThat(addOnInfoAndVersions.getVersions().last().getDownloadUri(),
				is("http://mavenrepo.openmrs.org/nexus/service/local/repositories/modules/content/org/openmrs/module/appui"
						+ "-omod/1.0/appui-omod-1.0.jar"));
		assertThat(addOnInfoAndVersions.getVersions().last().getRenameTo(), is("appui-1.0.omod"));
		
		assertThat(addOnInfoAndVersions.getVersions().first().getVersion().toString(), is("1.7"));
		assertThat(addOnInfoAndVersions.getVersions().first().getDownloadUri(),
				is("http://mavenrepo.openmrs.org/nexus/service/local/repositories/modules/content/org/openmrs/module/appui"
						+ "-omod/1.7/appui-omod-1.7.jar"));
		assertThat(addOnInfoAndVersions.getVersions().first().getRenameTo(), is("appui-1.7.omod"));
	}
	
	@Test
	public void testParsingConfigXmlForLanguages() throws Exception {
		OpenmrsMavenRepo backend = new OpenmrsMavenRepo();
		AddOnVersion version = new AddOnVersion();
		backend.handleConfigXml(loadXml("config.withNoRequirements.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), nullValue());
		assertThat(version.getRequireModules(), nullValue());
		assertThat(version.getSupportedLanguages(), contains("en", "fr", "de"));
	}
	
	@Test
	public void testParsingConfigXmlForRequiredOpenmrsVersion() throws Exception {
		OpenmrsMavenRepo backend = new OpenmrsMavenRepo();
		AddOnVersion version = new AddOnVersion();
		backend.handleConfigXml(loadXml("config.withRequiredVersion.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("1.11.3, 1.10.2 - 1.10.*, 1.9.9 - 1.9.*"));
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void testParsingConfigXmlForRequiredModuleVersion() throws Exception {
		OpenmrsMavenRepo backend = new OpenmrsMavenRepo();
		AddOnVersion version = new AddOnVersion();
		backend.handleConfigXml(loadXml("config.withRequiredModules.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("1.11.3, 1.10.2 - 1.10.*, 1.9.9 - 1.9.*"));
		assertThat(version.getRequireModules().size(), is(2));
		assertThat(version.getRequireModules(), hasEntry("org.openmrs.module.reporting", "${reportingModuleVersion}"));
		assertThat(version.getRequireModules(), hasEntry("org.openmrs.event", "?"));
	}
	
	private String loadXml(String filename) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
		return StreamUtils.copyToString(stream, Charset.defaultCharset());
	}
}