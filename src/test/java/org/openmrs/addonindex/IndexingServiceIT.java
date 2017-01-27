package org.openmrs.addonindex;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.backend.OpenmrsMavenRepo;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexingServiceIT {
	
	@MockBean
	ElasticSearchIndex elasticSearchIndex;
	
	@Autowired
	private IndexingService indexingService;
	
	@Test
	public void testBackendHandlersAreRegistered() throws Exception {
		AddOnToIndex toIndex = new AddOnToIndex();
		toIndex.setBackend(OpenmrsMavenRepo.class);
		BackendHandler handler = indexingService.getHandlerFor(toIndex);
		assertNotNull(handler);
		assertThat(handler, instanceOf(OpenmrsMavenRepo.class));
	}
}