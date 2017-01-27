package org.openmrs.addonindex.scheduled;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.springframework.util.StreamUtils;

public class FetchDetailsToIndexTest {
	
	@Test
	public void testParsingConfigXmlForLanguages() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex();
		AddOnVersion version = new AddOnVersion();
		task.handleConfigXml(loadXml("config.withNoRequirements.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), nullValue());
		assertThat(version.getRequireModules(), nullValue());
		assertThat(version.getSupportedLanguages(), contains("en", "fr", "de"));
	}
	
	@Test
	public void testParsingConfigXmlForRequiredOpenmrsVersion() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex();
		AddOnVersion version = new AddOnVersion();
		task.handleConfigXml(loadXml("config.withRequiredVersion.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("1.11.3, 1.10.2 - 1.10.*, 1.9.9 - 1.9.*"));
		assertThat(version.getRequireModules(), nullValue());
	}
	
	@Test
	public void testParsingConfigXmlForRequiredModuleVersion() throws Exception {
		FetchDetailsToIndex task = new FetchDetailsToIndex();
		AddOnVersion version = new AddOnVersion();
		task.handleConfigXml(loadXml("config.withRequiredModules.xml"), version);
		assertThat(version.getRequireOpenmrsVersion(), is("1.11.3, 1.10.2 - 1.10.*, 1.9.9 - 1.9.*"));
		assertThat(version.getRequireModules().size(), is(2));
		assertThat(version.getRequireModules(), hasItem(allOf(
				hasProperty("module", is("org.openmrs.module.reporting")),
				hasProperty("version", is("${reportingModuleVersion}"))
		)));
		assertThat(version.getRequireModules(), hasItem(allOf(
				hasProperty("module", is("org.openmrs.event")),
				hasProperty("version", is("?"))
		)));
	}
	
	private String loadXml(String filename) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
		return StreamUtils.copyToString(stream, Charset.defaultCharset());
	}
	
}