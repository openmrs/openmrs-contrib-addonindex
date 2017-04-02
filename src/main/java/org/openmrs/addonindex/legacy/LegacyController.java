package org.openmrs.addonindex.legacy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.OpenmrsVersionCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Supports functionality required by the OpenMRS Legacy UI module
 */
@Controller
public class LegacyController {
	
	@Autowired
	private Index index;
	
	@Autowired
	private IndexingService indexingService;
	
	/**
	 * These parameters are theoretically required but I don't think we need to handle them:
	 * <li>iSortingCols = 1, 2, 3, 4 for name, version, author, description
	 * <li>iSortCol_ =
	 * <li>iSortDir_ = asc/desc
	 *
	 * @param query
	 * @param start
	 * @param length
	 */
	@RequestMapping(method = RequestMethod.GET, value = "findModules")
	public LegacyFindModulesResponse findModules(
			@RequestParam(value = "sSearch", required = false) String query,
			@RequestParam(value = "iDisplayStart", defaultValue = "0") Integer start,
			@RequestParam(value = "iDisplayLength", defaultValue = "100") Integer length,
			@RequestParam(value = "sEcho", required = false) Integer sEcho,
			@RequestParam(value = "openmrs_version", required = false) String openmrsVersion,
			@RequestParam(value = "excludeModule", required = false) List<String> excludeModuleIds
	) throws Exception {
		Collection<AddOnInfoSummary> results = index.search(AddOnType.OMOD, query);
		
		LegacyFindModulesResponse response = new LegacyFindModulesResponse();
		response.setsEcho(sEcho);
		response.setiTotalRecords((int) indexingService.getAllToIndex()
				.getToIndex().stream().filter(i -> i.getType().equals(AddOnType.OMOD)).count());
		for (AddOnInfoSummary result : results) {
			if (shouldExclude(excludeModuleIds, result)) {
				continue;
			}
			AddOnInfoAndVersions full = index.getByUid(result.getUid());
			
			Optional<AddOnVersion> firstMatch = full.getVersions().stream()
					.filter(i ->
							OpenmrsVersionCompareUtil.matchRequiredVersions(openmrsVersion, i.getRequireOpenmrsVersion()))
					.findFirst();
			if (!firstMatch.isPresent()) {
				// no version of this module is suitable for the specified OpenMRS version
				continue;
			}
			
			AddOnVersion matchedVersion = firstMatch.get();
			
			String maintainer = full.getMaintainers() != null && full.getMaintainers().size() > 0 ?
					full.getMaintainers().get(0).getName() : "";
			
			response.addRow(
					matchedVersion.getDownloadUri(),
					result.getName(),
					matchedVersion.getVersion().toString(),
					maintainer,
					result.getDescription());
		}
		
		return response;
	}
	
	private boolean shouldExclude(List<String> excludeModuleIds, AddOnInfoSummary candidate) {
		if (excludeModuleIds == null) {
			return false;
		}
		// excludeModuleIds would just be "appui" but candidate.uid would be something like "org.openmrs.module.appui".
		// this is an imperfect hack to cover most cases
		String lookFor = candidate.getUid().startsWith("org.openmrs.module.") ?
				candidate.getUid().substring("org.openmrs.module.".length()) :
				candidate.getUid();
		return excludeModuleIds.contains(lookFor);
	}
	
}
