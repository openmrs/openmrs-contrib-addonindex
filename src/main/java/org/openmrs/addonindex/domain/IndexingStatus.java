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

public class IndexingStatus {
	
	private Map<String, Status> statuses = new LinkedHashMap<>();
	
	public Map<String, Status> getStatuses() {
		return statuses;
	}
	
	public void setStatus(AddOnToIndex toIndex, Status status) {
		statuses.put(toIndex.getUid(), status);
	}
	
	public Map<AddOnToIndex, Status> getStatusesFor(Collection<AddOnToIndex> toIndex) {
		LinkedHashMap<AddOnToIndex, Status> ret = new LinkedHashMap<>();
		for (AddOnToIndex i : toIndex) {
			ret.put(i, statuses.get(i.getUid()));
		}
		return ret;
	}
	
	public static class Status {
		
		private boolean indexingNow;
		
		private OffsetDateTime lastIndexed;
		
		private OffsetDateTime startedIndexing;
		
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
		
		public boolean isIndexingNow() {
			return indexingNow;
		}
		
		public void setIndexingNow(boolean indexingNow) {
			this.indexingNow = indexingNow;
		}
		
		public OffsetDateTime getLastIndexed() {
			return lastIndexed;
		}
		
		public void setLastIndexed(OffsetDateTime lastIndexed) {
			this.lastIndexed = lastIndexed;
		}
		
		public OffsetDateTime getStartedIndexing() {
			return startedIndexing;
		}
		
		public void setStartedIndexing(OffsetDateTime startedIndexing) {
			this.startedIndexing = startedIndexing;
		}
		
		public AddOnInfoSummary getSummary() {
			return summary;
		}
		
		public void setSummary(AddOnInfoSummary summary) {
			this.summary = summary;
		}
		
		public Exception getError() {
			return error;
		}
		
		public void setError(Exception error) {
			this.error = error;
		}
	}
	
}
