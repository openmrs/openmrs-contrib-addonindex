package org.openmrs.addonindex.scheduled;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.addonindex.backend.MavenRepoDetails;
import org.openmrs.addonindex.backend.OpenmrsMavenRepo;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.IndexingStatus;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FetchDetailsToIndexIT {
	
	@MockBean
	ElasticSearchIndex elasticSearchIndex;
	
	@MockBean
	private IndexingService indexingService;
	
	@MockBean
	private OpenmrsMavenRepo openmrsMavenRepo;
	
	@MockBean
	private RestTemplateBuilder restTemplateBuilder;
	
	@MockBean
	private RestTemplate restTemplate;
	
	@Mock
	IndexingStatus status;
	
	@Autowired
	private FetchDetailsToIndex task;
	
	private AddOnToIndex toIndex;
	
	public static final String DOWNLOAD_URI = "http://www.google.com";
	
	private AddOnInfoAndVersions infoAndVersions;
	
	@Before
	public void setUp() throws Exception {
		
		toIndex = new AddOnToIndex();
		toIndex.setUid("appui-omod");
		toIndex.setType(AddOnType.OMOD);
		toIndex.setBackend(OpenmrsMavenRepo.class);
		toIndex.setMavenRepoDetails(new MavenRepoDetails("org.openmrs.module", "appui-omod"));
		
		AddOnVersion v = new AddOnVersion();
		v.setVersion(new Version("1.0"));
		v.setDownloadUri(DOWNLOAD_URI);
		
		infoAndVersions = new AddOnInfoAndVersions();
		infoAndVersions.addVersion(v);
		
		given(indexingService.getHandlerFor(toIndex))
				.willReturn(openmrsMavenRepo);
		given(indexingService.getIndexingStatus())
				.willReturn(status);
		given(openmrsMavenRepo.getInfoAndVersionsFor(toIndex))
				.willReturn(infoAndVersions);
		given(restTemplateBuilder.build())
				.willReturn(restTemplate);
	}
	
	@Test
	public void testRecordingStatus() throws Exception {
		task.getDetailsAndIndex(toIndex);
		
		InOrder inOrder = Mockito.inOrder(status);
		inOrder.verify(status).setStatus(
				argThat(hasProperty("uid", is("appui-omod"))),
				argThat(hasProperty("indexingNow", is(true))));
		inOrder.verify(status).setStatus(
				argThat(hasProperty("uid", is("appui-omod"))),
				argThat(hasProperty("summary", hasProperty("versionCount", is(1)))));
	}
	
	@Test
	public void testGettingExtraDetailsByDefault() throws Exception {
		task.getDetailsAndIndex(toIndex);
		then(restTemplate).should().getForObject(DOWNLOAD_URI, Resource.class);
	}
	
	@Test
	public void testNotGettingExtraDetailsWhenConfigured() throws Exception {
		task.setFetchExtraDetails(false);
		task.getDetailsAndIndex(toIndex);
		then(restTemplate).should(never()).getForObject(DOWNLOAD_URI, Resource.class);
	}
	
}