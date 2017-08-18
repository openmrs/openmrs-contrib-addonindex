package org.openmrs.addonindex.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
		info.setDescription("Write code, save lives");
		info.addVersion(version);
		
		String json = mapper.writeValueAsString(info);
		AddOnInfoAndVersions parsed = mapper.readValue(json, AddOnInfoAndVersions.class);
		assertThat(parsed.getName(), is("OpenMRS"));
		assertThat(parsed.getDescription(), is("Write code, save lives"));
		assertThat(parsed.getVersions().size(), is(1));
		assertThat(parsed.getVersions().get(0).getVersion().toString(), is("1.0"));
	}

	@Test
    public void setDetailsBasedOnLatestVersion(){
        AddOnVersion version = new AddOnVersion();
        version.setVersion(new Version("1.0"));
        version.setModuleId("0");
        version.setModulePackage("org.openmrs.module.differentPackageName");

        AddOnVersion version2 = new AddOnVersion();
        version2.setVersion(new Version("2.0"));
        version2.setModuleId("1");
        version2.setModulePackage("org.openmrs.module.openmrs");

        AddOnInfoAndVersions info = new AddOnInfoAndVersions();
        info.setName("OpenMRS");
        info.setDescription("Write code, save lives");
        info.addVersion(version);
        info.addVersion(version2);

        info.setDetailsBasedOnLatestVersion();

        assertThat(info.getModuleId(), is("1"));
        assertThat(info.getModulePackage(), is("org.openmrs.module.openmrs"));
    }
	
}