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
		assertThat(toOpenmrsVersionRange(">= 2.44"), is("2.44.0"));
		assertThat(toOpenmrsVersionRange(">2.4.0"), is("2.4.0"));
		assertThat(toOpenmrsVersionRange("=1.2.3"), is("1.2.3"));
		assertThat(toOpenmrsVersionRange("v1.2.3"), is("1.2.3"));
	}
	
	@Test
	public void testIgnoringPrereleaseSuffixes() {
		// routes.json conventionally writes >=2.3.0-0, where -0 admits pre-release builds of the
		// minimum, and content.properties often carries -SNAPSHOT. OpenmrsVersionCompareUtil strips
		// any -qualifier before comparing, so dropping the suffix here loses nothing it could see.
		assertThat(toOpenmrsVersionRange(">=2.3.0-0"), is("2.3.0"));
		assertThat(toOpenmrsVersionRange("2.3.0-0"), is("2.3.0"));
		assertThat(toOpenmrsVersionRange("2.0.0-SNAPSHOT"), is("2.0.0"));
		assertThat(toOpenmrsVersionRange(">=2.4.0-SNAPSHOT"), is("2.4.0"));
		assertThat(toOpenmrsVersionRange("^2.4.0-0"), is("2.4.0 - 2.*"));
		assertThat(toOpenmrsVersionRange("^1.2.0-SNAPSHOT"), is("1.2.0 - 1.*"));
		assertThat(toOpenmrsVersionRange("^1.3.2-pre.1928"), is("1.3.2 - 1.*"));
		assertThat(toOpenmrsVersionRange("^0.2.3-0"), is("0.2.3 - 0.2.*"));
		assertThat(toOpenmrsVersionRange("~2.4.1-0"), is("2.4.1 - 2.4.*"));
	}
	
	@Test
	public void testConvertingPlainVersions() {
		assertThat(toOpenmrsVersionRange("1.2.0"), is("1.2.0"));
		assertThat(toOpenmrsVersionRange(" 1.2.0 "), is("1.2.0"));
		// partial and four-part versions never reach SemVer (which would read 1.0 as the whole
		// 1.0.x branch, and cannot parse 1.9.8.1 at all); a plain version means a minimum
		assertThat(toOpenmrsVersionRange("1.0"), is("1.0"));
		assertThat(toOpenmrsVersionRange("2.44"), is("2.44"));
		assertThat(toOpenmrsVersionRange("2"), is("2"));
		assertThat(toOpenmrsVersionRange("1.9.8.1"), is("1.9.8.1"));
	}
	
	@Test
	public void testConvertingCaretRanges() {
		assertThat(toOpenmrsVersionRange("^2"), is("2.0.0 - 2.*"));
		assertThat(toOpenmrsVersionRange("^2.4"), is("2.4.0 - 2.*"));
		assertThat(toOpenmrsVersionRange("^2.4.1"), is("2.4.1 - 2.*"));
		// on 0.x, a caret only allows the same minor
		assertThat(toOpenmrsVersionRange("^0.2.3"), is("0.2.3 - 0.2.*"));
		// semver4j normalizes ^0 to >=0.0.0 and loses the <1.0.0 bound, so ^0 is translated directly
		assertThat(toOpenmrsVersionRange("^0"), is("0.*"));
	}
	
	@Test
	public void testConvertingTildeRanges() {
		assertThat(toOpenmrsVersionRange("~2.4.1"), is("2.4.1 - 2.4.*"));
		assertThat(toOpenmrsVersionRange("~2.4"), is("2.4.0 - 2.4.*"));
		assertThat(toOpenmrsVersionRange("~2"), is("2.0.0 - 2.*"));
	}
	
	@Test
	public void testConvertingWildcards() {
		assertThat(toOpenmrsVersionRange("7.x"), is("7.0.0 - 7.*"));
		assertThat(toOpenmrsVersionRange("7.X"), is("7.0.0 - 7.*"));
		assertThat(toOpenmrsVersionRange("7.*"), is("7.0.0 - 7.*"));
		assertThat(toOpenmrsVersionRange("1.2.x"), is("1.2.0 - 1.2.*"));
	}
	
	@Test
	public void testRangesWithNoConstraint() {
		assertThat(toOpenmrsVersionRange(null), nullValue());
		assertThat(toOpenmrsVersionRange(""), nullValue());
		assertThat(toOpenmrsVersionRange("*"), nullValue());
		assertThat(toOpenmrsVersionRange("x"), nullValue());
	}
	
	@Test
	public void testConvertingBoundedRanges() {
		assertThat(toOpenmrsVersionRange(">=2.0.0 <4.0.0"), is("2.0.0 - 3.*"));
		assertThat(toOpenmrsVersionRange(">=6.0.0 <8.0.0"), is("6.0.0 - 7.*"));
		assertThat(toOpenmrsVersionRange(">3.3.0 <4.0.0"), is("3.3.0 - 3.*"));
		assertThat(toOpenmrsVersionRange(">=2.2.0 <2.4.0"), is("2.2.0 - 2.3.*"));
		assertThat(toOpenmrsVersionRange(">=1.2.0 <=2.0.0"), is("1.2.0 - 2.0.0"));
		assertThat(toOpenmrsVersionRange(">=2.0.0-0 <4.0.0-0"), is("2.0.0 - 3.*"));
		assertThat(toOpenmrsVersionRange(">=0.5.0 <1.0.0"), is("0.5.0 - 0.*"));
		// a SemVer hyphen range is inclusive on both ends; semver4j normalizes the upper bound to
		// <2.3.5, whose OpenMRS rendering 2.3.4.* still admits 2.3.4 and its sub-patch builds
		assertThat(toOpenmrsVersionRange("1.2.3 - 2.3.4"), is("1.2.3 - 2.3.4.*"));
	}
	
	@Test
	public void testRangesWeCannotExpress() {
		assertThat(toOpenmrsVersionRange(">=1.0.0 || ^2"), nullValue());
		// a bare upper bound has no lower edge to anchor an OpenMRS range on
		assertThat(toOpenmrsVersionRange("<2.0.0"), nullValue());
		// nothing sits below an upper bound of zero
		assertThat(toOpenmrsVersionRange(">=0.0.0 <0.0.0"), nullValue());
		assertThat(toOpenmrsVersionRange("${appuiVersion}"), nullValue());
		// semver4j throws NumberFormatException (not SemverException) for a part beyond
		// Integer.MAX_VALUE; it must come back as "cannot express", not escape to the caller
		assertThat(toOpenmrsVersionRange(">=2147483648.0.0"), nullValue());
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
		
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("3.9.0", toOpenmrsVersionRange(">=2.0.0 <4.0.0")),
		    is(true));
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("4.0.0", toOpenmrsVersionRange(">=2.0.0 <4.0.0")),
		    is(false));
		assertThat(OpenmrsVersionCompareUtil.matchRequiredVersions("1.9.0", toOpenmrsVersionRange(">=2.0.0 <4.0.0")),
		    is(false));
	}
}
