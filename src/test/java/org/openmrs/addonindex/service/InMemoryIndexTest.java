package org.openmrs.addonindex.service;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnType;

public class InMemoryIndexTest {
	
	@Test
	public void testIndexing() throws Exception {
		InMemoryIndex index = new InMemoryIndex();
		
		AddOnInfoAndVersions info = new AddOnInfoAndVersions();
		info.setUid("reporting-module");
		info.setName("Reporting Module");
		
		index.index(info);
		AddOnInfoAndVersions lookup = index.getByUid(info.getUid());
		assertThat(lookup, notNullValue());
		assertThat(lookup.getName(), is("Reporting Module"));
	}
	
	@Test
	public void testGetAllByType() throws Exception {
		InMemoryIndex index = new InMemoryIndex();
		
		AddOnInfoAndVersions yes = new AddOnInfoAndVersions();
		yes.setUid("yes");
		yes.setType(AddOnType.OMOD);
		
		AddOnInfoAndVersions no = new AddOnInfoAndVersions();
		no.setUid("no");
		no.setType(AddOnType.OWA);
		
		index.index(yes);
		index.index(no);
		
		Collection<AddOnInfoAndVersions> all = index.getAllByType(AddOnType.OMOD);
		assertThat(all, hasSize(1));
		assertThat(all, hasItem(yes));
	}
}