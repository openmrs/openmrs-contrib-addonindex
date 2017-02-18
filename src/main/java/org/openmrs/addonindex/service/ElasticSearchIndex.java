package org.openmrs.addonindex.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
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
			logger.info("Existing ES Index with " + result.getCount() + " documents");
		} else {
			// need to create the index
			handleError(client.execute(new CreateIndex.Builder(AddOnInfoAndVersions.ES_INDEX).build()));
			handleError(client.execute(new PutMapping.Builder(AddOnInfoAndVersions.ES_INDEX,
					AddOnInfoAndVersions.ES_TYPE,
					loadResource("elasticsearch/addOnInfoAndVersions-mappings.json")).build()));
		}
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
	public Collection<AddOnInfoSummary> search(AddOnType type, String query) throws IOException {
		
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (type != null) {
			queryBuilder.filter(QueryBuilders.matchQuery("type", type));
		}
		if (query != null) {
			queryBuilder.should(QueryBuilders.matchPhrasePrefixQuery("_all", query).slop(2).fuzziness("AUTO"));
			queryBuilder.should(QueryBuilders.prefixQuery("_all", query));
			queryBuilder.minimumNumberShouldMatch(1);
		}
		SearchResult result = client.execute(new Search.Builder(
				new SearchSourceBuilder().size(SEARCH_SIZE).query(queryBuilder).toString())
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
}
