package org.openmrs.addonindex.domain.artifactory;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AqlArtifactInfo {

	@Data
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Stats {

		private int downloads;

	}

	private String repo;

	private String path;

	private String name;

	private String created;

	private List<Stats> stats;

}
