package org.openmrs.addonindex.backend;

public class MavenRepoDetails {
	
	public MavenRepoDetails() {
	}
	
	public MavenRepoDetails(String groupId, String artifactId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
	}
	
	private String groupId;
	
	private String artifactId;
	
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getArtifactId() {
		return artifactId;
	}
	
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
}
