package org.openmrs.addonindex.legacy;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class LegacyControllerTest {
	
	@Test
	public void convertDownloadFilenameAsOpenmrsCoreHack_shouldConvertBintrayUrl() throws Exception {
		String original = "https://bintray.com/openmrs/omod/download_file?file_path=htmlformentry-3.4.0.omod";
		String expected = "https://dl.bintray.com/openmrs/omod/htmlformentry-3.4.0.omod";
		assertThat(new LegacyController().convertDownloadFilenameHack(original), is(expected));
	}
	
	@Test
	public void convertDownloadFilenameAsOpenmrsCoreHack_shouldNotChangeOtherUrl() throws Exception {
		String original = "https://openmrs.org/downloads/abc-3.4.0.omod";
		assertThat(new LegacyController().convertDownloadFilenameHack(original), is(original));
	}
}