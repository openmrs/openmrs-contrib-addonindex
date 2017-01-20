package org.openmrs.addonindex.util;

import com.fasterxml.jackson.annotation.JsonValue;

// Based on http://stackoverflow.com/a/11024200
// TODO need to handle non-number version components (perhaps just by ignoring them)
public class Version implements Comparable<Version> {
	
	private String version;
	
	public Version(String version) {
		this.version = version;
	}
	
	@JsonValue
	@Override
	public String toString() {
		return version;
	}
	
	@Override
	public int compareTo(Version that) {
		if (that == null) {
			return 1;
		}
		String[] thisParts = this.version.split("\\.");
		String[] thatParts = that.version.split("\\.");
		int length = Math.max(thisParts.length, thatParts.length);
		for (int i = 0; i < length; i++) {
			int thisPart = i < thisParts.length ?
					Integer.parseInt(thisParts[i]) : 0;
			int thatPart = i < thatParts.length ?
					Integer.parseInt(thatParts[i]) : 0;
			if (thisPart < thatPart) {
				return -1;
			}
			if (thisPart > thatPart) {
				return 1;
			}
		}
		return 0;
	}
	
	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		if (this.getClass() != that.getClass()) {
			return false;
		}
		return this.compareTo((Version) that) == 0;
	}
}
