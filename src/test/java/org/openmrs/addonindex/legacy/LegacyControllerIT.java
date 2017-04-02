package org.openmrs.addonindex.legacy;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.TestUtil;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.domain.Maintainer;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.Version;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LegacyControllerIT {
	
	@MockBean
	ElasticSearchIndex elasticSearchIndex;
	
	@MockBean
	IndexingService indexingService;
	
	@Autowired
	private LegacyController controller;
	
	@Before
	public void setUp() throws Exception {
		Maintainer darius = new Maintainer();
		
		AddOnVersion version1 = new AddOnVersion();
		version1.setVersion(new Version("1.0"));
		version1.setDownloadUri("https://modules.openmrs.org/modulus/api/releases/1367/download/appui-1.0.omod");
		version1.setRequireOpenmrsVersion("1.8.0");
		
		AddOnVersion version2 = new AddOnVersion();
		version2.setVersion(new Version("2.0"));
		version2.setDownloadUri("https://modules.openmrs.org/modulus/api/releases/1368/download/appui-2.0.omod");
		version2.setRequireOpenmrsVersion("1.12.5");
		
		darius.setName("darius");
		AddOnInfoAndVersions full = new AddOnInfoAndVersions();
		full.setUid("org.openmrs.module.appui");
		full.setType(AddOnType.OMOD);
		full.setName("App UI Module");
		full.setDescription("Support for using the App Framework's context in the UI Framework");
		full.setMaintainers(Collections.singletonList(darius));
		full.addVersion(version1);
		full.addVersion(version2);
		
		AddOnInfoSummary summary = new AddOnInfoSummary(full);
		
		given(elasticSearchIndex.search(AddOnType.OMOD, "appui"))
				.willReturn(Collections.singleton(summary));
		given(elasticSearchIndex.getByUid("org.openmrs.module.appui"))
				.willReturn(full);
		
		AddOnToIndex moduleToIndex = new AddOnToIndex();
		moduleToIndex.setType(AddOnType.OMOD);
		
		AllAddOnsToIndex allToIndex = new AllAddOnsToIndex();
		allToIndex.setToIndex(Arrays.asList(moduleToIndex, moduleToIndex));
		
		given(indexingService.getAllToIndex())
				.willReturn(allToIndex);
	}
	
	@Test
	public void testFindModulesWithoutVersion() throws Exception {
		LegacyFindModulesResponse response = controller.findModules("appui", 0, 0, 123, null, null);
		String json = new ObjectMapper().writeValueAsString(response);
		
		String expectedJson = TestUtil.getFileAsString("legacy-findModules.json");
		JSONAssert.assertEquals(expectedJson, json, false);
	}
	
	@Test
	public void testFindModulesWithOpenMrsVersion() throws Exception {
		LegacyFindModulesResponse response = controller.findModules("appui", 0, 0, 123, "1.9.3", null);
		String json = new ObjectMapper().writeValueAsString(response);
		
		String expectedJson = TestUtil.getFileAsString("legacy-findModules-old-version.json");
		JSONAssert.assertEquals(expectedJson, json, false);
	}
	
	@Test
	public void testFindModulesExcludeModule() throws Exception {
		LegacyFindModulesResponse response = controller.findModules("appui", 0, 0, 123, "1.9.3",
				Arrays.asList("appui"));
		String json = new ObjectMapper().writeValueAsString(response);
		
		String expectedJson = TestUtil.getFileAsString("legacy-findModules-exclude.json");
		JSONAssert.assertEquals(expectedJson, json, false);
	}
}