package org.openmrs.addonindex;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.util.StreamUtils;

public class TestUtil {
	
	public static String getFileAsString(String resourcePath) throws IOException {
		return StreamUtils.copyToString(TestUtil.class.getClassLoader().getResourceAsStream(resourcePath), Charset
				.defaultCharset());
	}
	
}
