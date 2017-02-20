package org.openmrs.addonindex.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.OffsetDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GsonConfigurationIT {
	
	@MockBean
	ElasticSearchIndex elasticSearchIndex;
	
	@Autowired
	private Gson gson;
	
	@Test
	public void testGson() throws Exception {
		AddOnVersion v = new AddOnVersion();
		v.setVersion(new Version("1.2.3"));
		v.setReleaseDatetime(OffsetDateTime.parse("2016-09-12T18:51:14.574Z"));
		v.addRequiredModule("org.openmrs.module.reporting", "1.0");
		
		AddOnInfoAndVersions a = new AddOnInfoAndVersions();
		a.setName("Testing");
		a.setType(AddOnType.OMOD);
		a.addVersion(v);
		
		String json = gson.toJson(a);
		
		assertTrue(json.indexOf("2016-09-12T18:51:14.574Z") > 0);
		
		AddOnInfoAndVersions converted = gson.fromJson(json, AddOnInfoAndVersions.class);
		assertThat(converted.getName(), is(a.getName()));
		assertThat(converted.getType(), is(AddOnType.OMOD));
		assertThat(converted.getVersions().get(0).getVersion().toString(), is(v.getVersion().toString()));
		assertThat(converted.getVersions().get(0).getReleaseDatetime(), equalTo(v.getReleaseDatetime()));
		assertThat(converted.getVersions().get(0).getRequireModules().get(0).getModule(), is("org.openmrs.module"
				+ ".reporting"));
		assertThat(converted.getVersions().get(0).getRequireModules().get(0).getVersion(), is("1.0"));
	}
	
}