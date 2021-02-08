package org.openmrs.addonindex.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

@JsonTest
public class AddOnInfoAndVersionsTest {
	
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testSerialization() throws Exception {
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("1.0"));
		
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setName("OpenMRS");
		info.setDescription("Write code, save lives");
		info.addVersion(version);
		
		String json = objectMapper.writeValueAsString(info);
		AddOnInfoAndVersions parsed = objectMapper.readValue(json, AddOnInfoAndVersions.class);
		assertThat(parsed.getName(), is("OpenMRS"));
		assertThat(parsed.getDescription(), is("Write code, save lives"));
		assertThat(parsed.getVersions().size(), is(1));
		assertThat(parsed.getVersions().get(0).getVersion().toString(), is("1.0"));
	}

	@Test
    public void testGetLatestMatchedVersion() throws Exception {
        AddOnInfoAndVersions info = new AddOnInfoAndVersions();
        info.setName("OpenMRS");
        info.setDescription("Write code, save lives");

        AddOnVersion latestVersion = new AddOnVersion();
        latestVersion.setVersion(new Version("2.0"));
        latestVersion.setRequireOpenmrsVersion("2.1.0");
        info.addVersion(latestVersion);

        AddOnVersion earlierVersion = new AddOnVersion();
        earlierVersion.setVersion(new Version("1.0"));
        earlierVersion.setRequireOpenmrsVersion("1.9.0");

        info.addVersion(latestVersion);
        info.addVersion(earlierVersion);

        assertThat(info.getLatestSupportedVersion("2.1.0").getVersion().toString(), is("2.0"));
        assertThat(info.getLatestSupportedVersion("2.0.0").getVersion().toString(), is("1.0"));
        assertThat(info.getLatestSupportedVersion("1.6.3"), nullValue());
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
