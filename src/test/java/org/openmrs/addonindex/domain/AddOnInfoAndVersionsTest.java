package org.openmrs.addonindex.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@JsonTest
public class AddOnInfoAndVersionsTest {
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private JacksonTester<AddOnInfoAndVersions> json;
	
	@Test
	public void testSerialization() throws Exception {
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("1.0"));
		
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setName("OpenMRS");
		info.addVersion(version);
		
		String json = mapper.writeValueAsString(info);
		AddOnInfoAndVersions parsed = mapper.readValue(json, AddOnInfoAndVersions.class);
		assertThat(parsed.getName(), is("OpenMRS"));
		assertThat(parsed.getVersions().size(), is(1));
		assertThat(parsed.getVersions().first().getVersion().toString(), is("1.0"));
	}
	
}