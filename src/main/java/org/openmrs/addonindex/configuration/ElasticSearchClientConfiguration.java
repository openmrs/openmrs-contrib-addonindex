package org.openmrs.addonindex.configuration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfiguration {

	@Autowired
	@Bean
	public RestHighLevelClient restHighLevelClient(@Value("${elasticsearch.url}") String url) {
		return new RestHighLevelClient(RestClient.builder(HttpHost.create(url)));
	}
}
