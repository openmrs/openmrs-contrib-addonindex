package org.openmrs.addonindex.service;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.openmrs.addonindex.domain.AddOnInfoAndVersions;
import org.openmrs.addonindex.domain.AddOnInfoSummary;
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
	
	@Test
	public void testGetByTag() throws Exception {
		InMemoryIndex index = new InMemoryIndex();
		
		AddOnInfoAndVersions yes = new AddOnInfoAndVersions();
		yes.setUid("yes");
		yes.addTag("form-entry");
		yes.setType(AddOnType.OMOD);
		
		AddOnInfoAndVersions no = new AddOnInfoAndVersions();
		no.setUid("no");
		yes.addTag("reporting");
		no.setType(AddOnType.OWA);
		
		index.index(yes);
		index.index(no);
		
		Collection<AddOnInfoAndVersions> withTag = index.getByTag("form-entry");
		assertThat(withTag, hasSize(1));
		assertThat(withTag, hasItem(yes));
	}
	
	@Test
	public void testSearch() throws Exception {
		InMemoryIndex index = new InMemoryIndex();
		
		AddOnInfoAndVersions yes = new AddOnInfoAndVersions();
		yes.setUid("reporting-module");
		yes.setName("Reporting Module");
		yes.setType(AddOnType.OMOD);
		
		AddOnInfoAndVersions no1 = new AddOnInfoAndVersions();
		no1.setUid("idgen-module");
		no1.setName("Id Gen Module");
		no1.setType(AddOnType.OMOD);
		
		AddOnInfoAndVersions no2 = new AddOnInfoAndVersions();
		no2.setUid("reporting-owa");
		no2.setName("Reporting OWA");
		no2.setType(AddOnType.OWA);
		
		index.index(yes);
		index.index(no1);
		index.index(no2);
		
		Collection<AddOnInfoSummary> results = index.search(AddOnType.OMOD, "reporting");
		assertThat(results, hasSize(1));
		assertThat(results, hasItem(hasProperty("uid", is("reporting-module"))));
	}
}