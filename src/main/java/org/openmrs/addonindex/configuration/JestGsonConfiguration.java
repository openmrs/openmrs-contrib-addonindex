package org.openmrs.addonindex.configuration;

import org.apache.http.HttpHost;
import org.openmrs.addonindex.util.Version;
import org.openmrs.addonindex.util.VersionTypeConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.elasticsearch.jest.JestProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 * We include this configuration because Jest's default Gson configuration can't handle our Version class.
 * JestAutoConfiguration will look at GsonAutoconfiguration (and won't let us override the Gson bean) so we define both
 * Gson and JestClient here to override that.
 */
@Configuration
public class JestGsonConfiguration {
	
	private final JestProperties properties;
	
	private final ObjectProvider<Gson> gsonProvider;
	
	public JestGsonConfiguration(JestProperties properties,
	                             ObjectProvider<Gson> gsonProvider) {
		this.properties = properties;
		this.gsonProvider = gsonProvider;
	}
	
	@Bean
	public Gson gson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Version.class, new VersionTypeConverter());
		return builder.create();
	}
	
	@Bean(
			destroyMethod = "shutdownClient"
	)
	@DependsOn("gson")
	public JestClient jestClient() {
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(this.createHttpClientConfig());
		return factory.getObject();
	}
	
	// copied from JestAutoConfiguration
	protected HttpClientConfig createHttpClientConfig() {
		HttpClientConfig.Builder builder = new HttpClientConfig.Builder(
				this.properties.getUris());
		if (StringUtils.hasText(this.properties.getUsername())) {
			builder.defaultCredentials(this.properties.getUsername(),
					this.properties.getPassword());
		}
		String proxyHost = this.properties.getProxy().getHost();
		if (StringUtils.hasText(proxyHost)) {
			Integer proxyPort = this.properties.getProxy().getPort();
			Assert.notNull(proxyPort, "Proxy port must not be null");
			builder.proxy(new HttpHost(proxyHost, proxyPort));
		}
		Gson gson = this.gsonProvider.getIfUnique();
		if (gson != null) {
			builder.gson(gson);
		}
		return builder.connTimeout(this.properties.getConnectionTimeout())
				.readTimeout(this.properties.getReadTimeout()).build();
	}
	
}
