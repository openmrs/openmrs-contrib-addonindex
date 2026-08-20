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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semver4j.Semver;
import org.semver4j.range.Range;
import org.semver4j.range.RangeList;
import org.semver4j.range.RangeListFactory;

/**
 * Translates SemVer version ranges into the OpenMRS version-range syntax understood by
 * {@link OpenmrsVersionCompareUtil}. Content packages and frontend modules express their
 * requirements as SemVer ranges (e.g. {@code >=2.4.0}, {@code ^2}), whereas everything else
 * expresses them in the OpenMRS syntax (e.g. {@code 2.4.0}, {@code 2.*}, {@code 2.0.0 - 2.*}).
 *
 * <p>semver4j does the SemVer parsing: it normalizes every range form (caret, tilde, x-ranges,
 * hyphen ranges) into one or two comparators, which are then rendered in OpenMRS syntax here.
 * Pre-release suffixes are dropped when {@code format(Semver)} writes the OpenMRS string.
 */
public class VersionRangeConverter {
	
	/**
	 * A version with no range operator, translated directly rather than through SemVer: SemVer would
	 * read a partial version ({@code 1.0}) as the whole 1.0.x branch, and cannot parse a four-part
	 * OpenMRS version ({@code 1.9.8.1}) at all, but a manifest that lists a plain version means "that
	 * version and above". A pre-release suffix is dropped, as everywhere else.
	 */
	private static final Pattern PLAIN_VERSION = Pattern.compile("^v?(\\d+(?:\\.\\d+)*)(?:-[0-9A-Za-z.-]+)?$");
	
	private VersionRangeConverter() {
	}
	
	/**
	 * Translates a SemVer range into the equivalent OpenMRS version range.
	 * <table>
	 * <caption>Examples of the translation</caption>
	 * <tr>
	 * <td>{@code >=2.4.0}</td>
	 * <td>{@code 2.4.0}</td>
	 * <td>that version and above</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^2.4.1}</td>
	 * <td>{@code 2.4.1 - 2.*}</td>
	 * <td>from that version to the end of the 2.x branch</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ~2.4.1}</td>
	 * <td>{@code 2.4.1 - 2.4.*}</td>
	 * <td>from that version to the end of the 2.4.x branch</td>
	 * </tr>
	 * <tr>
	 * <td>{@code 7.x}</td>
	 * <td>{@code 7.0.0 - 7.*}</td>
	 * <td>the 7.x branch only</td>
	 * </tr>
	 * </table>
	 *
	 * <p>Note that a bare {@code 1.2.0} means exactly that version in SemVer, but "that version and
	 * above" in OpenMRS. We keep the looser OpenMRS reading, on the assumption that a manifest listing
	 * a plain version means a minimum. {@code >} is likewise kept as the looser inclusive minimum,
	 * since OpenMRS syntax has no exclusive bound.
	 *
	 * @param semVerRange a SemVer range, as found in {@code content.properties} or {@code routes.json}
	 * @return the equivalent OpenMRS version range, or {@code null} if there is no constraint to
	 *         express or we cannot express it
	 */
	public static String toOpenmrsVersionRange(String semVerRange) {
		String range = semVerRange == null ? "" : semVerRange.trim();
		if (range.isEmpty()) {
			return null;
		}
		
		Matcher plain = PLAIN_VERSION.matcher(range);
		if (plain.matches()) {
			return plain.group(1);
		}
		
		if ("^0".equals(range)) {
			// semver4j normalizes ^0 to >=0.0.0 and loses the <1.0.0 bound, so translate it directly
			return "0.*";
		}
		
		RangeList rangeList;
		try {
			rangeList = RangeListFactory.create(range);
		}
		catch (IllegalArgumentException ex) {
			// covers semver4j's SemverException for unparseable input, and the NumberFormatException
			// it lets escape for a numeric part beyond Integer.MAX_VALUE (e.g. a date-stamp version)
			return null;
		}
		
		if (rangeList.isSatisfiedByAny()) {
			// *, x, >=0.0.0: no constraint to express
			return null;
		}
		
		List<List<Range>> orBranches = rangeList.get();
		if (orBranches.size() != 1) {
			// a || union has no OpenMRS equivalent we can rely on
			return null;
		}
		
		List<Range> ranges = orBranches.get(0);
		if (ranges.size() == 1) {
			return fromSingleBound(ranges.get(0));
		}
		if (ranges.size() == 2) {
			return fromBoundedRange(ranges.get(0), ranges.get(1));
		}
		return null;
	}
	
	private static String fromSingleBound(Range range) {
		if (isUpperBound(operatorOf(range))) {
			// a bare upper bound has no lower edge to anchor an OpenMRS range on
			return null;
		}
		return format(range.getRangeVersion());
	}
	
	/**
	 * A bounded range becomes OpenMRS's inclusive dash range: the upper bound stays as-is when
	 * inclusive ({@code <=}), and becomes the wildcard branch just below it when exclusive
	 * ({@code <}).
	 */
	private static String fromBoundedRange(Range first, Range second) {
		Range.RangeOperator firstOperator = operatorOf(first);
		Range.RangeOperator secondOperator = operatorOf(second);
		Range lower;
		Range upper;
		Range.RangeOperator upperOperator;
		if (isLowerBound(firstOperator) && isUpperBound(secondOperator)) {
			lower = first;
			upper = second;
			upperOperator = secondOperator;
		} else if (isLowerBound(secondOperator) && isUpperBound(firstOperator)) {
			lower = second;
			upper = first;
			upperOperator = firstOperator;
		} else {
			return null;
		}
		String top = upperOperator == Range.RangeOperator.LTE ? format(upper.getRangeVersion())
		        : wildcardBelow(upper.getRangeVersion());
		if (top == null) {
			return null;
		}
		return format(lower.getRangeVersion()) + " - " + top;
	}
	
	private static boolean isLowerBound(Range.RangeOperator operator) {
		return operator == Range.RangeOperator.GT || operator == Range.RangeOperator.GTE;
	}
	
	private static boolean isUpperBound(Range.RangeOperator operator) {
		return operator == Range.RangeOperator.LT || operator == Range.RangeOperator.LTE;
	}
	
	/**
	 * semver4j 6.0.0 keeps a Range's operator private, exposing it only through {@code toString()},
	 * which is specified as the operator's symbol followed by the version. Stripping the version off
	 * leaves the symbol, which the library's own {@link Range.RangeOperator#value} parses (an empty
	 * string is {@code EQ}).
	 */
	private static Range.RangeOperator operatorOf(Range range) {
		String rendered = range.toString();
		String version = range.getRangeVersion().toString();
		return Range.RangeOperator.value(rendered.substring(0, rendered.length() - version.length()));
	}
	
	/**
	 * The inclusive OpenMRS upper bound just below an exclusive SemVer one: everything under
	 * {@code 4.0.0} is {@code 3.*}, everything under {@code 2.4.0} is {@code 2.3.*}. Returns
	 * {@code null} for a bound of zero, which nothing sits below.
	 */
	private static String wildcardBelow(Semver bound) {
		if (bound.getPatch() > 0) {
			return bound.getMajor() + "." + bound.getMinor() + "." + (bound.getPatch() - 1) + ".*";
		}
		if (bound.getMinor() > 0) {
			return bound.getMajor() + "." + (bound.getMinor() - 1) + ".*";
		}
		if (bound.getMajor() > 0) {
			return (bound.getMajor() - 1) + ".*";
		}
		return null;
	}
	
	private static String format(Semver version) {
		// dropping any pre-release suffix, which OpenmrsVersionCompareUtil ignores anyway
		return version.withClearedPreReleaseAndBuild().getVersion();
	}
}
