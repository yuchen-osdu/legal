/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.legal.ibm.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * @author mbayser
 *
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class LegalTagTests {

	private CloudantClient cloudant;
	private static final String databaseNameFormat = "legal-tag-test-%s";
	private String databaseName;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	private Database db;

	private CloudantLegalTagRepository repo;


	public LegalTagTests() {
	}

    @Before
    public void setup() throws Exception {

    	databaseName = String.format(databaseNameFormat, dtf.format(LocalDateTime.now()));

    	IBMCloudantClientFactory fact;
    	ServiceCredentials creds;
    	try {
    		 fact = new IBMCloudantClientFactory();
    		 creds = ServiceCredentials.environmentSupplied();
    	} catch (Exception e) {
    		 fact = new IBMCloudantClientFactory(System.getenv("CLOUDANT_URL"), System.getenv("CLOUDANT_KEY"));
    		 creds = new ServiceCredentials(System.getenv("CLOUDANT_URL"), System.getenv("CLOUDANT_KEY"));
    	}

    	fact.getGsonBuilder()
			.registerTypeAdapter(java.sql.Date.class,          CloudantBackedLegalTag.sqlDateSerializer)
			.registerTypeAdapter(java.sql.Date.class,          CloudantBackedLegalTag.sqlDateDeserializer)
			.registerTypeAdapter(LegalTag.class,               CloudantBackedLegalTag.serializer)
			.registerTypeAdapter(CloudantBackedLegalTag.class, CloudantBackedLegalTag.serializer)
			.registerTypeAdapter(CloudantBackedLegalTag.class, CloudantBackedLegalTag.deserializer);
    	cloudant = fact.getClient();

        db = fact.getDatabase("test",databaseName);
        db.getDBUri();

    	repo = new CloudantLegalTagRepository(creds, "test", databaseName);

    }

    @After
    public void teardown() throws Exception {
    	cloudant.deleteDB(IBMCloudantClientFactory.dbNameRule("test", databaseName));
    }

    @Test
    public void testEquals() {

    	CloudantBackedLegalTag original = (CloudantBackedLegalTag)configureTag(new CloudantBackedLegalTag());

    	CloudantBackedLegalTag copy1 = (CloudantBackedLegalTag)configureTag(new CloudantBackedLegalTag());
    	assertEquals(original, copy1);

    	copy1.set_rev("qflkhfeiurlew");

    	assertEquals(original, copy1);

    	LegalTag copy2 = configureTag(new LegalTag());

    	assertTrue(original.equals(copy2));
    	assertTrue(copy2.equals(original));
    }

    @SuppressWarnings("deprecation")
	@Test
    public void testSerialization() {

    	CloudantBackedLegalTag original = new CloudantBackedLegalTag();
    	original.setId(1001L);
    	original.set_rev("3-rev");
    	original.setName("TENANT1");
    	original.setDescription("description");
    	original.setIsValid(true);

    	Properties props = new Properties();
    	props.setCountryOfOrigin(Arrays.asList("Erebor"));
    	props.setDataType("gem");
    	props.setSecurityClassification("bestows the right to rule");
    	props.setPersonalData("Belongs to the Folk of Durin");
    	props.setExportClassification("Kings Jewel");
    	props.setOriginator("Thorin Oakenshield");
    	props.setContractId("Burglary contract");
    	props.setExpirationDate(new Date(2941, 11, 32));

    	original.setProperties(props);

    	Gson gson = new GsonBuilder()
    	    	.registerTypeAdapter(CloudantBackedLegalTag.class, CloudantBackedLegalTag.serializer)
    	    	.registerTypeAdapter(LegalTag.class, CloudantBackedLegalTag.serializer)
    	    	.registerTypeAdapter(CloudantBackedLegalTag.class, CloudantBackedLegalTag.deserializer)
    	    	.create();

    	JsonElement serialized = gson.toJsonTree(original);
    	System.out.println(serialized);

    	CloudantBackedLegalTag deserialized = gson.fromJson(serialized, CloudantBackedLegalTag.class);

    	assertEquals(original.getId(),                           deserialized.getId());
    	assertEquals(original.getName(),                         deserialized.getName());
    	assertEquals(original.getDescription(),                  deserialized.getDescription());
    	assertEquals(original.getIsValid(),                      deserialized.getIsValid());
    	assertEquals(original.getProperties(),                   deserialized.getProperties());

    	// Just to make sure that lombok is doing its job
    	assertEquals(original.hashCode(),                        deserialized.hashCode());
    	assertEquals(original, deserialized);
    }

    @Test
    public void testCreate() throws Exception {

    	LegalTag tag1 = createValidLegalTag("tag1");
    	Long id1 = tag1.getId();

    	Long saved_id1 = repo.create(tag1);

    	assertEquals(id1, saved_id1);


    	try {
        	LegalTag nullIdtag = createValidLegalTag("nulltag");
        	nullIdtag.setId(null);
    		repo.create(nullIdtag);
    		fail("Expected exception");
    	} catch (NullPointerException e) {	}


    	LegalTag tag2 = createValidLegalTag("tag2");
    	tag2.setIsValid(true);
    	Long id2 = tag2.getId();

    	Long saved_id2 = repo.create(tag2);

    	assertEquals(id2, saved_id2);

    }

    @Test
    public void testGet() throws Exception {
    	LegalTag tag1 = createValidLegalTag("tag1");
    	Long id1 = tag1.getId();

    	Long saved_id1 = repo.create(tag1);
    	assertEquals(id1, saved_id1);

    	LegalTag tag2 = createValidLegalTag("tag2");
    	tag2.setIsValid(true);
    	Long id2 = tag2.getId();

    	Long saved_id2 = repo.create(tag2);

    	assertEquals(id2, saved_id2);

    	Collection<LegalTag> retrv1 = repo.get(new long[] { id1 });
    	assertEquals(retrv1.size(), 1);

    	LegalTag retrv1_tag1 = retrv1.iterator().next();
    	assertEquals(tag1, retrv1_tag1);
    }

    @Test
    public void testNone() throws Exception {

    	LegalTag tag1 = createValidLegalTag("tag1");
    	Long id1 = tag1.getId();

    	Long saved_id1 = repo.create(tag1);
    	assertEquals(id1, saved_id1);

    	LegalTag tag2 = createValidLegalTag("tag2");
    	tag2.setIsValid(true);
    	Long id2 = tag2.getId();

    	Long saved_id2 = repo.create(tag2);

    	assertEquals(id2, saved_id2);

    	Collection<LegalTag> retrv1 = repo.get(new long[] { 4324L });
    	assertEquals(retrv1.size(), 0);
    }


    @Test
    public void testGetMultiple() throws Exception {

    	LegalTag tag1 = createValidLegalTag("tag1");
    	Long id1 = tag1.getId();

    	Long saved_id1 = repo.create(tag1);
    	assertEquals(id1, saved_id1);

    	LegalTag tag2 = createValidLegalTag("tag2");
    	tag2.setIsValid(true);
    	Long id2 = tag2.getId();

    	Long saved_id2 = repo.create(tag2);

    	assertEquals(id2, saved_id2);

    	Collection<LegalTag> retrv1 = repo.get(new long[] { id1, id2 });
    	assertEquals(retrv1.size(), 2);

    	Iterator<LegalTag> it = retrv1.iterator();

    	LegalTag retrv1_tag1 = it.next();
    	assertEquals(tag1, retrv1_tag1);
    	LegalTag retrv1_tag2 = it.next();
    	assertEquals(tag2, retrv1_tag2);
    }

    @Test
    public void testUpdate() throws Exception {

    	LegalTag tag1 = createValidLegalTag("tag1");
    	Long id1 = tag1.getId();
    	tag1.setDescription("description1");

    	try {
    		repo.update(tag1);
    		fail("expected exception when updating object not present in the database");
    	} catch (IllegalArgumentException ex) {}

    	Long saved_id1 = repo.create(tag1);
    	assertEquals(id1, saved_id1);

    	Collection<LegalTag> retrv1 = repo.get(new long[] { id1 });
    	assertEquals(retrv1.size(), 1);

    	Iterator<LegalTag> retrv1_it = retrv1.iterator();

    	LegalTag retrv1_tag1 = retrv1_it.next();
    	assertEquals(tag1, retrv1_tag1);

    	retrv1_tag1.setDescription("description2");

    	// This update will work without extra logic because retrv1_tag1 is
    	// actually a CloudantBackedLegalTag with a valid _rev
    	repo.update(retrv1_tag1);

    	Collection<LegalTag> retrv2 = repo.get(new long[] { id1 });
    	assertEquals(retrv2.size(), 1);

    	Iterator<LegalTag> retrv2_it = retrv2.iterator();

    	LegalTag retrv2_tag1 = retrv2_it.next();

    	assertNotEquals(tag1, retrv2_tag1);
    	assertEquals(retrv1_tag1, retrv2_tag1);


    	LegalTag tag1_equivalent = createValidLegalTag("tag1");
    	tag1.setDescription("description3");
    	assertEquals(id1, tag1_equivalent.getId());

    	// This update would fail it the update method didn't handle the missing _rev
    	repo.update(tag1_equivalent);


    	// This update would fail it the update method didn't handle the outdated _rev
    	repo.update(retrv2_tag1);

    }

    @Test
    public void testDelete () throws Exception {

    	LegalTag tag1 = createValidLegalTag("tag1");
    	Long id1 = tag1.getId();
    	tag1.setDescription("description1");

  		assertFalse(repo.delete(tag1));

    	CloudantBackedLegalTag cloudantBacked = (CloudantBackedLegalTag)configureTag(new CloudantBackedLegalTag());
    	assertFalse(repo.delete(cloudantBacked));

    	Long saved_id1 = repo.create(tag1);
    	assertEquals(id1, saved_id1);

    	Collection<LegalTag> retrv1 = repo.get(new long[] { id1 });
    	assertEquals(retrv1.size(), 1);

    	assertTrue(repo.delete(tag1));

    	Collection<LegalTag> retrv2 = repo.get(new long[] { id1 });
    	assertEquals(retrv2.size(), 0);

    	repo.create(tag1);

    	Collection<LegalTag> retrv3 = repo.get(new long[] { id1 });
    	assertEquals(retrv3.size(), 1);

    	Iterator<LegalTag> retrv3_it = retrv3.iterator();

    	LegalTag retrv3_tag1 = retrv3_it.next();
    	assertEquals(tag1, retrv3_tag1);

    	retrv3_tag1.setDescription("description2");
    	assertNotEquals(tag1, retrv3_tag1);

    	repo.update(retrv3_tag1);

    	Collection<LegalTag> retrv4 = repo.get(new long[] { id1 });
    	assertEquals(retrv4.size(), 1);


    	// This would fail without the retry on conflict logic
    	assertTrue(repo.delete(retrv3_tag1));

    	Collection<LegalTag> retrv5 = repo.get(new long[] { id1 });
    	assertEquals(retrv5.size(), 0);

    }

    @Test
    public void testList() throws Exception {

    	LegalTag tag1 = createValidLegalTag("tag1");
    	tag1.setIsValid(false);
    	repo.create(tag1);
    	LegalTag tag2 = createValidLegalTag("tag2");
    	tag2.setIsValid(true);
    	repo.create(tag2);
    	LegalTag tag3 = createValidLegalTag("tag3");
    	tag3.setIsValid(false);
    	repo.create(tag3);
    	LegalTag tag4 = createValidLegalTag("tag4");
    	tag4.setIsValid(true);
    	repo.create(tag4);
    	LegalTag tag5 = createValidLegalTag("tag5");
    	tag5.setIsValid(false);
    	repo.create(tag5);
    	LegalTag tag6 = createValidLegalTag("tag6");
    	tag6.setIsValid(true);
    	repo.create(tag6);

    	ListLegalTagArgs allTagsNoPaging = new ListLegalTagArgs();
    	allTagsNoPaging.setIsValid(null);
    	allTagsNoPaging.setLimit(0);

    	Set<String> allNames = repo.list(allTagsNoPaging).stream().map(t -> t.getName()).collect(Collectors.toSet());

    	assertEquals(6,  allNames.size());
    	assertTrue(allNames.contains("tag1"));
    	assertTrue(allNames.contains("tag2"));
    	assertTrue(allNames.contains("tag3"));
    	assertTrue(allNames.contains("tag4"));
    	assertTrue(allNames.contains("tag5"));
    	assertTrue(allNames.contains("tag6"));


    	ListLegalTagArgs validTagsNoPaging = new ListLegalTagArgs();
    	validTagsNoPaging.setIsValid(true);
    	validTagsNoPaging.setLimit(0);

    	Set<String> validNames = repo.list(validTagsNoPaging).stream().map(t -> t.getName()).collect(Collectors.toSet());

    	assertEquals(3, validNames.size());
    	assertTrue(validNames.contains("tag2"));
    	assertTrue(validNames.contains("tag4"));
    	assertTrue(validNames.contains("tag6"));

    	HashSet<String> allNamesSet = new HashSet<>(Arrays.asList(
    			"tag1",
    			"tag2",
    			"tag3",
    			"tag4",
    			"tag5",
    			"tag6"
		));

    	ListLegalTagArgs allTagsWithPaging = new ListLegalTagArgs();
    	allTagsWithPaging.setIsValid(null);
    	allTagsWithPaging.setLimit(1);

    	assertEquals(6, allNamesSet.size());

    	List<String> page1 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(1, page1.size());
    	allNamesSet.remove(page1.get(0));
    	assertEquals(5, allNamesSet.size());

    	List<String> page2 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(1, page2.size());
    	allNamesSet.remove(page2.get(0));
    	assertEquals(4, allNamesSet.size());

    	List<String> page3 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(1, page3.size());
    	allNamesSet.remove(page3.get(0));
    	assertEquals(3, allNamesSet.size());

    	List<String> page4 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(1, page4.size());
    	allNamesSet.remove(page4.get(0));
    	assertEquals(2, allNamesSet.size());

    	List<String> page5 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(1, page5.size());
    	allNamesSet.remove(page5.get(0));
    	assertEquals(1, allNamesSet.size());

    	List<String> page6 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(1, page6.size());
    	allNamesSet.remove(page6.get(0));
    	assertEquals(0, allNamesSet.size());

    	List<String> page7 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(0, page7.size());

    	List<String> page8 = repo.list(allTagsWithPaging).stream().map(t -> t.getName()).collect(Collectors.toList());
    	assertEquals(0, page8.size());
    }

    @SuppressWarnings("deprecation")
	public static LegalTag configureTag(LegalTag original) {

    	original.setId(1001L);
    	original.setName("TENANT1");
    	original.setDescription("description");
    	original.setIsValid(true);

    	Properties props = new Properties();
    	props.setCountryOfOrigin(Arrays.asList("Erebor"));
    	props.setDataType("gem");
    	props.setSecurityClassification("bestows the right to rule");
    	props.setPersonalData("Belongs to the Folk of Durin");
    	props.setExportClassification("King's Jewel");
    	props.setOriginator("Thorin Oakenshield");
    	props.setContractId("Burglary contract");
    	props.setExpirationDate(new Date(2941, 11, 32));
    	original.setProperties(props);
    	return original;
    }

    public static LegalTag createValidLegalTag(String name){
    	LegalTag legalTag = new LegalTag();
        legalTag.setProperties(createValidProperties());
        legalTag.setName(name);
        legalTag.setIsValid(false);
        legalTag.setDefaultId();
        return legalTag;
    }
    @SuppressWarnings("serial")
	public static Properties createValidProperties(){
        Properties properties = new Properties();
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("USA");}});
        properties.setExpirationDate(new Date(System.currentTimeMillis()));
        properties.setOriginator("MyCompany");
        properties.setContractId("Unknown");
        properties.setDataType("Tranferred Data");
        properties.setPersonalData("Sensitive Personal Information");
        properties.setSecurityClassification("Confidential");
        properties.setExportClassification("ECCN");
        return properties;
    }
}
