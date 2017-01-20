package org.openmrs.addonindex;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.openmrs.addonindex.domain.AddOnToIndex;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IndexingServiceTest {
	
	@Test
	public void testLoading() throws Exception {
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("toIndex.json");
		ObjectMapper jackson = new ObjectMapper();
		List<AddOnToIndex> list = jackson.readValue(resourceStream,
				jackson.getTypeFactory().constructCollectionType(List.class, AddOnToIndex.class));
		assertThat(list.get(0).getMavenRepoDetails().getGroupId(), is("org.openmrs.module"));
		assertThat(list.get(0).getMavenRepoDetails().getArtifactId(), is("reporting-omod"));
	}
	
}