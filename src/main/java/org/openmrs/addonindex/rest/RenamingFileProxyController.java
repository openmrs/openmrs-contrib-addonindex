/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.rest;

import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.service.Index;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Modules in the OpenMRS Maven Repository are named (module)-omod-(version).jar but upon download
 * they need to be renamed as (module)-version.omod. We aren't hosting these files within this
 * application, so we can't use the `download` attribute of a link to implement this. So instead we
 * provide a way to download them by proxy, renaming along the way. We limit this to only point to
 * the OpenMRS Maven Repository to avoid abuse.
 */
@Controller
public class RenamingFileProxyController {
	
	private final RestTemplateBuilder restTemplateBuilder;
	
	private final Index index;
	
	@Autowired
	public RenamingFileProxyController(RestTemplateBuilder restTemplateBuilder, Index index) {
		this.restTemplateBuilder = restTemplateBuilder;
		this.index = index;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/api/v1/addon/{uid}/{version}/download")
	public void downloadWithCorrectName(@PathVariable("uid") String addonUid, @PathVariable("version") String version,
	        HttpServletResponse response) throws Exception {
		
		AddOnInfoAndVersions addOn = index.getByUid(addonUid);
		if (addOn == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		Optional<AddOnVersion> addOnVersion = addOn.getVersion(new Version(version));
		if (addOnVersion.isEmpty()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		if (addOnVersion.get().getRenameTo() == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File name not set");
			return;
		}
		
		String fileName = addOnVersion.get().getRenameTo();
		// streams the upstream body straight through
		restTemplateBuilder.build().execute(addOnVersion.get().getDownloadUri(), HttpMethod.GET, null, upstream -> {
			response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
			    ContentDisposition.attachment().filename(fileName).build().toString());
			MediaType contentType = upstream.getHeaders().getContentType();
			if (contentType != null) {
				response.setContentType(contentType.toString());
			}
			long contentLength = upstream.getHeaders().getContentLength();
			if (contentLength >= 0) {
				response.setContentLengthLong(contentLength);
			}
			StreamUtils.copy(upstream.getBody(), response.getOutputStream());
			return null;
		});
	}
	
}
