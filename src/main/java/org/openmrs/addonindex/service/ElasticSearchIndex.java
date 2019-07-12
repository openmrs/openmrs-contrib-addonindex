/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.addonindex.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.common.settings.Settings;

import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.openmrs.addonindex.domain.AddOnStatus;
import org.openmrs.addonindex.domain.AddOnType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Count;
import io.searchbox.core.CountResult;
import io.searchbox.core.Get;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;

@Repository
public class ElasticSearchIndex implements Index {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final int SEARCH_SIZE = 200;
	
	private final int TOP_DOWNLOADS_SIZE = 20;
	
	private JestClient client;
	
	@Autowired
	public ElasticSearchIndex(JestClient client) {
		this.client = client;
	}
	
	@PostConstruct
	public void setUp() throws IOException {
		CountResult result = client.execute(new Count.Builder()
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		if (result.isSucceeded()) {
			logger.info("Existing ES index with " + result.getCount() + " documents");
		} else {
			// need to create the index
			logger.info("Creating new ES index: " + AddOnInfoAndVersions.ES_INDEX);
			handleError(client.execute(new CreateIndex.Builder(AddOnInfoAndVersions.ES_INDEX).settings(Settings.builder()
					.loadFromPath(
							Paths.get("elasticsearch/addOnInfoAndVersions-settings.json")
					).build().getAsMap()).build()));
			handleError(client.execute(new CreateIndex.Builder(AddOnInfoAndVersions.ES_INDEX).build()));
		}
		logger.info("Updating mappings on ES index");
		handleError(client.execute(new PutMapping.Builder(AddOnInfoAndVersions.ES_INDEX,
				AddOnInfoAndVersions.ES_TYPE,
				loadResource("elasticsearch/addOnInfoAndVersions-mappings.json"))
				.build()));
	}
	
	private String loadResource(String name) throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
		return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
	}
	
	private void handleError(JestResult result) {
		if (!result.isSucceeded()) {
			throw new IllegalStateException("Jest Error: " + result.getErrorMessage());
		}
	}
	
	@Override
	public void index(AddOnInfoAndVersions infoAndVersions) throws IOException {
		handleError(client.execute(new io.searchbox.core.Index.Builder(infoAndVersions)
				.index(AddOnInfoAndVersions.ES_INDEX)
				.type(AddOnInfoAndVersions.ES_TYPE)
				.build()));
	}
	
	@Override
	public Collection<AddOnInfoSummary> search(AddOnType type, String query, String tag, String uid,
											   String name, String exclude, AddOnStatus status) throws IOException {
		BoolQueryBuilder boolQB = QueryBuilders.boolQuery();
		if (name != null) {
			//Exact match on name
			boolQB.filter(QueryBuilders.matchQuery("name.raw", name));
		}
		if (uid != null) {
			//Exact match on moduleID
			boolQB.filter(QueryBuilders.matchQuery("moduleId", uid));
		}
		if (status != null) {
			//Exact match on status
			boolQB.filter(QueryBuilders.matchQuery("status", status));
		}
		if (type != null) {
			//Exact match on type
			boolQB.filter(QueryBuilders.matchQuery("type", type));
		}
		if (tag != null) {	
			boolQB.filter(QueryBuilders.matchQuery("tags", tag));
		}
		if (query != null) {
			//Exact match on id(Highest priority)
			boolQB.should(QueryBuilders.termQuery("_id", query).boost(1700.0f));

			//Exact match on tag(High priority)
			boolQB.should(QueryBuilders.termQuery("tags", query).boost(1500.0f));

			//Prefix match of module name(Medium priority)
			boolQB.should(QueryBuilders.prefixQuery("name", query).boost(4.0f));

			//Query is subset of module name(Medium priority)
			boolQB.should(QueryBuilders.matchQuery("name", query).boost(2.0f));

			//Description matches query either completely or partially(Low priority)
			boolQB.should(QueryBuilders.matchQuery("description", query).boost(0.5f));

			//Allow for spelling mistake while searching for a particular module
			boolQB.should(QueryBuilders.matchPhraseQuery("name", query).slop(2).fuzziness("AUTO"));
			boolQB.minimumNumberShouldMatch(1);
		}

		if (exclude != null) {
			boolQB.mustNot(QueryBuilders.multiMatchQuery(exclude , "name", "tags", "description",
					"moduleId", "status", "_id", "type"));
		}
		
		BoostingQueryBuilder boostingQB = QueryBuilders.boostingQuery();
		boostingQB.positive(boolQB);

		//Decrease ranking of those modules which are "Deprecated" or "Inactive"
		boostingQB.negative(QueryBuilders.termsQuery("status", "DEPRECATED", "INACTIVE"));
		boostingQB.negativeBoost(0.01f);
		
		SearchResult result = client.execute(new Search.Builder(
				new SearchSourceBuilder().size(SEARCH_SIZE).query(boostingQB).toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		return result.getHits(AddOnInfoAndVersions.class).stream()
				.map(sr -> new AddOnInfoSummary(sr.source))
				.collect(Collectors.toList());
	}
	
	@Override
	public Collection<AddOnInfoAndVersions> getAllByType(AddOnType type) throws IOException {
		SearchResult result = client.execute(new Search.Builder(new SearchSourceBuilder()
				.size(SEARCH_SIZE)
				.query(QueryBuilders.matchQuery("type", type)).toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		return result.getHits(AddOnInfoAndVersions.class).stream()
				.map(sr -> sr.source)
				.collect(Collectors.toList());
	}
	
	@Override
	public AddOnInfoAndVersions getByUid(String uid) throws IOException {
		return client.execute(new Get.Builder(AddOnInfoAndVersions.ES_INDEX, uid).build())
				.getSourceAsObject(AddOnInfoAndVersions.class);
	}

	@Override
	public AddOnInfoAndVersions getByModulePackage(String modulePackage) throws IOException {
		SearchResult result = client.execute(new Search.Builder(new SearchSourceBuilder()
				.size(1)
				.query(QueryBuilders.matchQuery("modulePackage", modulePackage)).toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());

		List<AddOnInfoAndVersions> modules = result.getHits(AddOnInfoAndVersions.class).stream()
                .map(sr -> sr.source)
                .collect(Collectors.toList());

		if (modules.isEmpty()) {
            return null;
        }
        return modules.get(0);
    }
	
	@Override
	public Collection<AddOnInfoAndVersions> getByTag(String tag) throws Exception {
		SearchResult result = client.execute(new Search.Builder(new SearchSourceBuilder()
				.size(SEARCH_SIZE)
				.query(QueryBuilders.matchQuery("tags", tag)).toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		return result.getHits(AddOnInfoAndVersions.class).stream()
				.map(sr -> sr.source)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<AddOnInfoSummaryAndStats> getTopDownloaded() throws Exception {
		SearchResult result = client.execute(new Search.Builder(new SearchSourceBuilder()
				.size(TOP_DOWNLOADS_SIZE)
				.sort("downloadCountInLast30Days", SortOrder.DESC)
				.query(QueryBuilders.matchAllQuery()).toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		return result.getHits(AddOnInfoAndVersions.class).stream()
				.map(sr -> new AddOnInfoSummaryAndStats(sr.source))
				.collect(Collectors.toList());
	}
	
}
