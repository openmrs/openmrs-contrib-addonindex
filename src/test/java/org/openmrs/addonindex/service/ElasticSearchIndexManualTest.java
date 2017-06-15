package org.openmrs.addonindex.service;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * This test will write to your live elasticsearch database
 */
@Ignore
@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchIndexManualTest {
	
	@Autowired
	private ElasticSearchIndex elasticSearchIndex;
	
	@Autowired
	private JestClient client;
	
	@Test
	public void testIndex() throws Exception {
		AddOnVersion v = new AddOnVersion();
		v.setVersion(new Version("1.0"));
		v.setDownloadUri("http://www.google.com");
		v.addLanguage("en");
		
		AddOnInfoAndVersions a = new AddOnInfoAndVersions();
		a.setUid("testing-module");
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
		
		Collection<AddOnInfoSummary> results = elasticSearchIndex.search(AddOnType.OMOD, "testing",null);
		assertThat(allByType.size(), greaterThanOrEqualTo(1));
	}
	
	@Test
	public void testSearch() throws Exception {
		SearchResult result = client.execute(new Search.Builder(new SearchSourceBuilder()
				.query(QueryBuilders.matchPhrasePrefixQuery("_all", "dictionary conc").slop(2).fuzziness("AUTO"))
				.toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		System.out.println("Hits: " + result.getTotal());
		for (String s : result.getSourceAsStringList()) {
			System.out.println(s);
		}
	}

	@Test
	public void testTag() throws Exception {
		SearchResult result = client.execute(new Search.Builder(new SearchSourceBuilder()
				.query(QueryBuilders.matchQuery("tags", "form-entry"))
				.toString())
				.addIndex(AddOnInfoAndVersions.ES_INDEX)
				.build());
		System.out.println("Hits: " + result.getTotal());
		for (String s : result.getSourceAsStringList()) {
			System.out.println(s);
		}
	}
}
