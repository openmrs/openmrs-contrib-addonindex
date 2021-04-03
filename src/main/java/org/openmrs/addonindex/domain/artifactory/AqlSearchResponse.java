package org.openmrs.addonindex.domain.artifactory;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AqlSearchResponse {

	private List<AqlArtifactInfo> results;

}
