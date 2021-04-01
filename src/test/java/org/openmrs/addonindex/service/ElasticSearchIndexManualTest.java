package org.openmrs.addonindex.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnInfoSummaryAndStats;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test will write to your live elasticsearch database
 */
@Disabled
@SpringBootTest
public class ElasticSearchIndexManualTest {

	@Autowired
	private ElasticSearchIndex elasticSearchIndex;

	@Autowired
	private RestHighLevelClient client;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testIndex() throws IOException {
		AddOnVersion v = new AddOnVersion();
		v.setVersion(new Version("1.0"));
		v.setDownloadUri("http://www.google.com");
		v.addLanguage("en");

		AddOnInfoAndVersions a = new AddOnInfoAndVersions();
		a.setUid("testing-module");
		a.setModulePackage("testing-mod");
		a.setModuleId("1");
		a.setType(AddOnType.OMOD);
		a.setName("Testing ES");
		a.setDescription("This is a test");
		a.addVersion(v);

		elasticSearchIndex.index(a);

		AddOnInfoAndVersions byUid = elasticSearchIndex.getByUid(a.getUid());
		assertThat(byUid.getName(), is(a.getName()));
		assertThat(byUid.getVersions().get(0).getVersion().toString(), is("1.0"));

		Collection<AddOnInfoAndVersions> allByType = elasticSearchIndex.getAllByType(AddOnType.OMOD);
		assertThat(allByType.size(), greaterThanOrEqualTo(1));

		Collection<AddOnInfoSummary> results = elasticSearchIndex.search(AddOnType.OMOD, "testing", null);
		assertThat(results.size(), greaterThanOrEqualTo(1));
	}

	@Test
	public void testSearch() throws IOException {
		SearchResponse response = client.search(new SearchRequest(AddOnInfoAndVersions.ES_INDEX)
						.source(new SearchSourceBuilder().query(QueryBuilders.fuzzyQuery("_all", "dictionary conc"))),
				RequestOptions.DEFAULT);
		System.out.println("Hits: " + response.getHits().getTotalHits());
		for (SearchHit hit : response.getHits()) {
			System.out.println(hit.getSourceAsString());
		}
	}

	@Test
	public void testGetByModulePackage() throws IOException {
		SearchResponse response = client.search(new SearchRequest(AddOnInfoAndVersions.ES_INDEX)
				.source(new SearchSourceBuilder()
						.size(1)
						.query(QueryBuilders.matchQuery("modulePackage", "testing-mod"))), RequestOptions.DEFAULT);

		SearchResponse responseNotFound = client.search(new SearchRequest(AddOnInfoAndVersions.ES_INDEX)
				.source(new SearchSourceBuilder()
						.size(1)
						.query(QueryBuilders.matchQuery("modulePackage", "fake-mod"))), RequestOptions.DEFAULT);

		List<AddOnInfoAndVersions> searchResponse = Arrays.stream(response.getHits().getHits())
				.filter(SearchHit::hasSource)
				.map(hit -> objectMapper.convertValue(hit.getSourceAsMap(), AddOnInfoAndVersions.class))
				.collect(Collectors.toList());

		List<AddOnInfoAndVersions> searchResponseNotFound = Arrays.stream(responseNotFound.getHits().getHits())
				.filter(SearchHit::hasSource)
				.map(hit -> objectMapper.convertValue(hit.getSourceAsMap(), AddOnInfoAndVersions.class))
				.collect(Collectors.toList());

		assertThat(searchResponse, notNullValue());
		assertThat(searchResponse, not(empty()));
		assertThat(searchResponseNotFound, empty());
		System.out.println("Module Package Name: " + searchResponse.get(0).getModulePackage());
	}

	@Test
	public void testTag() throws IOException {
		SearchResponse response = client.search(new SearchRequest(AddOnInfoAndVersions.ES_INDEX)
			.source(new SearchSourceBuilder()
				.query(QueryBuilders.matchQuery("tags", "form-entry"))), RequestOptions.DEFAULT);

		System.out.println("Hits: " + response.getHits().getTotalHits());
		for (SearchHit hit : response.getHits().getHits()) {
			System.out.println(hit.getSourceAsString());
		}
	}

	@Test
	public void testTopDownloaded() throws Exception {
		List<AddOnInfoSummaryAndStats> top = elasticSearchIndex.getTopDownloaded();
		for (AddOnInfoSummaryAndStats a : top) {
			System.out.println(a.getDownloadCount() + "\t" + a.getSummary().getName());
		}
	}
}
