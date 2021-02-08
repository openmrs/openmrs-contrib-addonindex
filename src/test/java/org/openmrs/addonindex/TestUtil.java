package org.openmrs.addonindex;

import java.io.IOException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.springframework.util.StreamUtils;

public class TestUtil {
	
	public static String getFileAsString(String resourcePath) throws IOException {
		return StreamUtils.copyToString(TestUtil.class.getClassLoader().getResourceAsStream(resourcePath), Charset
				.defaultCharset());
	}
	
	public static void loadLocalAddOnsToIndex(ObjectMapper objectMapper, IndexingService indexingService) throws
			IOException {
		String json = getFileAsString("add-ons-to-index.json");
		AllAddOnsToIndex allAddOns = objectMapper.readValue(json, AllAddOnsToIndex.class);
		indexingService.setAllToIndex(allAddOns);
	}
	
}
