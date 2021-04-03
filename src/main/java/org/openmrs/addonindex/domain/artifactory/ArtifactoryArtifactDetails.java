package org.openmrs.addonindex.domain.artifactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactoryArtifactDetails {

	private String created;

	private String downloadUri;

}
