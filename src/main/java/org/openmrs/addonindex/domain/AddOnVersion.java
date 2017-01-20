package org.openmrs.addonindex.domain;

import org.openmrs.addonindex.util.Version;

public class AddOnVersion implements Comparable<AddOnVersion> {
	
	private Version version;
	
	private String downloadUri;
	
	private String renameTo;
	
	public Version getVersion() {
		return version;
	}
	
	public void setVersion(Version version) {
		this.version = version;
	}
	
	public String getDownloadUri() {
		return downloadUri;
	}
	
	public void setDownloadUri(String downloadUri) {
		this.downloadUri = downloadUri;
	}
	
	public String getRenameTo() {
		return renameTo;
	}
	
	public void setRenameTo(String renameTo) {
		this.renameTo = renameTo;
	}
	
	@Override
	public int compareTo(AddOnVersion other) {
		if (other == null) {
			return -1;
		}
		return this.getVersion().compareTo(other.getVersion());
	}
}
