package org.openmrs.addonindex.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class VersionTest {

	@Test
	public void testCompare() {
		assertThat(new Version("1.9"), lessThan(new Version("1.10")));
		assertThat(new Version("1.0.1"), greaterThan(new Version("1.0")));
		assertThat(new Version("1.0"), comparesEqualTo(new Version("1.0.0")));
	}

	@Test
	public void testAlphaBeta() {
		List<Version> list = Arrays.asList(
				new Version("1.0.0"),
				new Version("1.0.0-RC"),
				new Version("1.0.0-beta.2"),
				new Version("1.0.0-beta"),
				new Version("1.0.0-alpha"),
				new Version("0.9")
		);

		Collections.sort(list);
		assertThat(list.get(0).toString(), is("0.9"));
		assertThat(list.get(1).toString(), is("1.0.0-alpha"));
		assertThat(list.get(2).toString(), is("1.0.0-beta"));
		assertThat(list.get(3).toString(), is("1.0.0-beta.2"));
		assertThat(list.get(4).toString(), is("1.0.0-RC"));
		assertThat(list.get(5).toString(), is("1.0.0"));
	}
}
