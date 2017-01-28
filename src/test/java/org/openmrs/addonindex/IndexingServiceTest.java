package org.openmrs.addonindex;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.addonindex.backend.OpenmrsMavenRepo;
import org.openmrs.addonindex.domain.AddOnToIndex;
import org.openmrs.addonindex.domain.AllAddOnsToIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The idea is that the add-ons-to-index.json file represents all the modules we want to index, and the application will
 * periodically refresh it from github. Further, we expect module authors to submit pull requests modifying that file
 * in order to add their own modules for indexing. This class will test for various scenarios to ensure the file remains
 * well-formed, consistent, and doesn't violate any constraints. (CI should report if any PRs violate these tests.)
 *
 * @throws Exception
 */
@RunWith(SpringRunner.class)
@JsonTest
public class IndexingServiceTest {
	
	@Autowired
	private ObjectMapper mapper;
	
	private AllAddOnsToIndex toIndex;
	
	@Before
	public void setUp() throws Exception {
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("add-ons-to-index.json");
		toIndex = mapper.readValue(resourceStream, AllAddOnsToIndex.class);
	}
	
	@Test
	public void testParseable() throws Exception {
		assertThat(toIndex.size(), not(0));
		assertThat(toIndex.getToIndex().get(0).getName(), notNullValue());
	}
	
	@Test
	public void testUidPresentAndUnique() throws Exception {
		Set<String> already = new HashSet<>();
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			assertNotNull("UID is required", addOn.getUid());
			assertFalse("UID is not unique:" + addOn.getUid(), already.contains(addOn.getUid()));
			already.add(addOn.getUid());
		}
	}
	
	@Test
	public void testNamesAndDescriptionPresentAndNotTooLong() throws Exception {
		for (AddOnToIndex addOn : toIndex.getToIndex()) {
			assertThat(addOn.getName(), notNullValue());
			assertThat(addOn.getName().length(), lessThan(100));
			if (addOn.getBackend().equals(OpenmrsMavenRepo.class)) {
				assertThat(addOn.getDescription(), notNullValue());
				assertThat(addOn.getDescription().length(), lessThan(1000));
			}
		}
	}
}