package org.openmrs.addonindex;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.backend.BackendHandler;
import org.openmrs.addonindex.backend.OpenmrsMavenRepo;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnList;
import org.openmrs.addonindex.domain.AddOnReference;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AddOnType;
import org.openmrs.addonindex.domain.AddOnVersion;
import org.openmrs.addonindex.domain.MaterializedAddOnList;
import org.openmrs.addonindex.service.ElasticSearchIndex;
import org.openmrs.addonindex.service.IndexingService;
import org.openmrs.addonindex.util.Version;
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
	
	@Test
	public void testMaterialize() throws Exception {
		AddOnReference reference = new AddOnReference();
		reference.setUid("org.openmrs.module.xforms");
		
		AddOnList list = new AddOnList();
		list.setUid("uid");
		list.setName("Name");
		list.setDescription("Description");
		list.setAddOns(Arrays.asList(reference));
		
		AddOnVersion version = new AddOnVersion();
		version.setVersion(new Version("2.0"));
		
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setUid("org.openmrs.module.xforms");
		info.setName("XForms");
		info.setDescription("");
		info.setType(AddOnType.OMOD);
		info.addVersion(version);
		
		when(elasticSearchIndex.getByUid("org.openmrs.module.xforms")).thenReturn(info);
		
		MaterializedAddOnList materialized = indexingService.materialize(list);
		
		verify(elasticSearchIndex).getByUid("org.openmrs.module.xforms");
		assertThat(materialized.getUid(), is("uid"));
		assertThat(materialized.getName(), is("Name"));
		assertThat(materialized.getDescription(), is("Description"));
		assertThat(materialized.getAddOns(), hasSize(1));
		assertThat(materialized.getAddOns().get(0).getDetails().getUid(), is(info.getUid()));
		assertThat(materialized.getAddOns().get(0).getDetails().getLatestVersion(), is(version.getVersion()));
	}
	
}