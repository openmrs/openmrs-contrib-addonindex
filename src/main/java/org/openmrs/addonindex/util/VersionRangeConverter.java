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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Content packages express their requirements as SemVer ranges (e.g. {@code >=2.4.0}, {@code ^2}),
 * whereas everything else we index expresses them in the syntax understood by
 * {@link OpenmrsVersionCompareUtil} (e.g. {@code 2.4.0}, {@code 2.*}). This translates the former
 * into the latter so that a single comparator can be used for both.
 * <p>
 * {@link OpenmrsVersionCompareUtil#compareVersion(String, String)} splits on {@code .} and parses
 * each part as a number, falling back to zero when that fails, so an untranslated range like
 * {@code >=2.4.0} silently compares as {@code 0.4.0} and matches every version. Anything we can't
 * translate must therefore be reported as unknown rather than passed through.
 */
public class VersionRangeConverter {
	
	// A pre-release suffix on a version (-0, -SNAPSHOT, -pre.1928) is dropped everywhere:
	// OpenmrsVersionCompareUtil strips any -qualifier before comparing, so the suffix can't affect
	// the translated range anyway. The suffix never contains whitespace, which keeps SemVer hyphen
	// ranges ("1.2.3 - 2.3.4") from matching as a version with a suffix.
	private static final String PRERELEASE = "(?:-[0-9A-Za-z.-]+)?";
	
	private static final Pattern CARET = Pattern.compile("^\\^\\s*(\\d+)(?:\\.(\\d+))?(?:\\.\\d+)?" + PRERELEASE + "$");
	
	private static final Pattern TILDE = Pattern.compile("^~\\s*(\\d+)(?:\\.(\\d+))?(?:\\.\\d+)?" + PRERELEASE + "$");
	
	private static final Pattern WILDCARD = Pattern.compile("^(\\d+(?:\\.\\d+)*)\\.[xX*]$");
	
	private static final Pattern MINIMUM = Pattern.compile("^(?:(?:>=|>|=)\\s*)?v?(\\d+(?:\\.\\d+)*)" + PRERELEASE + "$");
	
	private static final Pattern BOUNDED = Pattern.compile(
	    "^(?:>=|>)\\s*v?(\\d+(?:\\.\\d+)*)" + PRERELEASE + "\\s+<(=)?\\s*v?(\\d+(?:\\.\\d+)*)" + PRERELEASE + "$");
	
	private VersionRangeConverter() {
	}
	
	/**
	 * Translates a SemVer range into the equivalent OpenMRS version range.
	 * <table>
	 * <tr>
	 * <td>{@code >=2.4.0}</td>
	 * <td>{@code 2.4.0}</td>
	 * <td>that version and above</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^2}</td>
	 * <td>{@code 2.*}</td>
	 * <td>the 2.x branch only</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ~2.4.1}</td>
	 * <td>{@code 2.4.*}</td>
	 * <td>the 2.4.x branch only</td>
	 * </tr>
	 * <tr>
	 * <td>{@code 7.x}</td>
	 * <td>{@code 7.*}</td>
	 * <td>the 7.x branch only</td>
	 * </tr>
	 * </table>
	 * <p>
	 * Note that a bare {@code 1.2.0} means exactly that version in SemVer, but "that version and above"
	 * in OpenMRS. We keep the looser OpenMRS reading, on the assumption that a content package listing
	 * a plain version means a minimum.
	 *
	 * @param semVerRange a SemVer range, as found in content.properties
	 * @return the equivalent OpenMRS version range, or null if there is no constraint to express or we
	 *         cannot express it
	 */
	public static String toOpenmrsVersionRange(String semVerRange) {
		if (semVerRange == null) {
			return null;
		}
		
		String range = semVerRange.trim();
		if (range.isEmpty() || "*".equals(range) || "x".equalsIgnoreCase(range)) {
			return null;
		}
		
		// ^2 and ^2.4.1 both allow the whole 2.x branch, but on 0.x every release may break, so ^0.2.3
		// allows only the 0.2.x branch
		Matcher caret = CARET.matcher(range);
		if (caret.matches()) {
			if ("0".equals(caret.group(1)) && caret.group(2) != null) {
				return "0." + caret.group(2) + ".*";
			}
			return caret.group(1) + ".*";
		}
		
		Matcher tilde = TILDE.matcher(range);
		if (tilde.matches()) {
			return tilde.group(2) == null ? tilde.group(1) + ".*" : tilde.group(1) + "." + tilde.group(2) + ".*";
		}
		
		Matcher wildcard = WILDCARD.matcher(range);
		if (wildcard.matches()) {
			return wildcard.group(1) + ".*";
		}
		
		Matcher minimum = MINIMUM.matcher(range);
		if (minimum.matches()) {
			return minimum.group(1);
		}
		
		// a bounded range becomes OpenMRS's inclusive dash range: the upper bound stays as-is when
		// inclusive (<=), and becomes the wildcard branch just below it when exclusive (<)
		Matcher bounded = BOUNDED.matcher(range);
		if (bounded.matches()) {
			String upper = bounded.group(2) != null ? bounded.group(3) : wildcardBelow(bounded.group(3));
			if (upper != null) {
				return bounded.group(1) + " - " + upper;
			}
		}
		
		// || unions and bare upper bounds have no OpenMRS equivalent we can rely on
		return null;
	}
	
	/**
	 * The inclusive OpenMRS upper bound just below an exclusive SemVer one: everything under 4.0.0 is
	 * 3.*, everything under 2.4.0 is 2.3.*. Returns null for a bound of zero, which nothing sits below.
	 */
	private static String wildcardBelow(String bound) {
		String[] parts = bound.split("\\.");
		int last = parts.length - 1;
		while (last > 0 && "0".equals(parts[last])) {
			last--;
		}
		long edge = Long.parseLong(parts[last]);
		if (edge == 0) {
			return null;
		}
		StringBuilder below = new StringBuilder();
		for (int i = 0; i < last; i++) {
			below.append(parts[i]).append('.');
		}
		return below.append(edge - 1).append(".*").toString();
	}
}
