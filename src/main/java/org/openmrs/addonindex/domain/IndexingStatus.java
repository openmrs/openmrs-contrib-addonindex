/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.domain;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

public class IndexingStatus {

	private final Map<String, Status> statuses = new LinkedHashMap<>();

	public Map<String, Status> getStatuses() {
		return statuses;
	}

	public void setStatus(AddOnToIndex toIndex, Status status) {
		statuses.put(toIndex.getUid(), status);
	}

	public Map<AddOnToIndex, Status> getStatusesFor(Collection<AddOnToIndex> toIndex) {
		return toIndex.stream()
				.collect(Collectors.toMap(a -> a, a -> statuses.get(a.getUid()), (a, b) -> a, LinkedHashMap::new));
	}

	@Data
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	@NoArgsConstructor
	public static class Status {

		private boolean indexingNow;

		private OffsetDateTime lastIndexed;

		private OffsetDateTime startedIndexing;

		@EqualsAndHashCode.Include
		private AddOnInfoSummary summary;

		private Exception error;

		public static Status success(AddOnInfoSummary summary) {
			Status status = new Status();
			status.lastIndexed = OffsetDateTime.now();
			status.summary = summary;
			return status;
		}

		public static Status error(Exception error) {
			Status status = new Status();
			status.lastIndexed = OffsetDateTime.now();
			status.error = error;
			return status;
		}

		public static Status indexingNow() {
			Status status = new Status();
			status.startedIndexing = OffsetDateTime.now();
			status.indexingNow = true;
			return status;
		}
	}

}
