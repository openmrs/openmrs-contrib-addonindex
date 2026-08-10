/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.addonindex.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.openmrs.addonindex.util.VersionRangeConverter.toOpenmrsVersionRange;

public class VersionRangeConverterTest {
	
	@Test
	public void testConvertingMinimumVersions() {
		assertThat(toOpenmrsVersionRange(">=2.4.0"), is("2.4.0"));
		assertThat(toOpenmrsVersionRange(">= 2.44"), is("2.44"));
		assertThat(toOpenmrsVersionRange(">2.4.0"), is("2.4.0"));
		assertThat(toOpenmrsVersionRange("=1.2.3"), is("1.2.3"));
		assertThat(toOpenmrsVersionRange("v1.2.3"), is("1.2.3"));
	}
	
	@Test
	public void testConvertingPlainVersions() {
		assertThat(toOpenmrsVersionRange("1.2.0"), is("1.2.0"));
		assertThat(toOpenmrsVersionRange(" 1.2.0 "), is("1.2.0"));
	}
	
	@Test
	public void testConvertingCaretRanges() {
		assertThat(toOpenmrsVersionRange("^2"), is("2.*"));
		assertThat(toOpenmrsVersionRange("^2.4"), is("2.*"));
		assertThat(toOpenmrsVersionRange("^2.4.1"), is("2.*"));
		// on 0.x, a caret only allows the same minor
		assertThat(toOpenmrsVersionRange("^0.2.3"), is("0.2.*"));
		assertThat(toOpenmrsVersionRange("^0"), is("0.*"));
	}
	
	@Test
	public void testConvertingTildeRanges() {
		assertThat(toOpenmrsVersionRange("~2.4.1"), is("2.4.*"));
		assertThat(toOpenmrsVersionRange("~2.4"), is("2.4.*"));
		assertThat(toOpenmrsVersionRange("~2"), is("2.*"));
	}
	
	@Test
	public void testConvertingWildcards() {
		assertThat(toOpenmrsVersionRange("7.x"), is("7.*"));
		assertThat(toOpenmrsVersionRange("7.X"), is("7.*"));
		assertThat(toOpenmrsVersionRange("7.*"), is("7.*"));
		assertThat(toOpenmrsVersionRange("1.2.x"), is("1.2.*"));
	}
	
	@Test
	public void testRangesWithNoConstraint() {
		assertThat(toOpenmrsVersionRange(null), nullValue());
		assertThat(toOpenmrsVersionRange(""), nullValue());
		assertThat(toOpenmrsVersionRange("*"), nullValue());
		assertThat(toOpenmrsVersionRange("x"), nullValue());
	}
	
	@Test
	public void testRangesWeCannotExpress() {
		assertThat(toOpenmrsVersionRange(">=1.0.0 || ^2"), nullValue());
		assertThat(toOpenmrsVersionRange(">=1.2.0 <2.0.0"), nullValue());
		assertThat(toOpenmrsVersionRange("<2.0.0"), nullValue());
		assertThat(toOpenmrsVersionRange("2.0.0-SNAPSHOT"), nullValue());
		assertThat(toOpenmrsVersionRange("${appuiVersion}"), nullValue());
	}
	
	/**
	 * The point of converting at all: OpenmrsVersionCompareUtil can act on the result, whereas the
	 * SemVer range it came from parses as nonsense and matches everything.
	 */
	@Test
	public void testConvertedRangesCompareCorrectly() {
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("1.9.0", ">=2.4.0"), is(true));
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("1.9.0", toOpenmrsVersionRange(">=2.4.0")), is(false));
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("2.5.0", toOpenmrsVersionRange(">=2.4.0")), is(true));
		
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("2.5.0", toOpenmrsVersionRange("^2")), is(true));
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("3.0.0", toOpenmrsVersionRange("^2")), is(false));
	}
}
