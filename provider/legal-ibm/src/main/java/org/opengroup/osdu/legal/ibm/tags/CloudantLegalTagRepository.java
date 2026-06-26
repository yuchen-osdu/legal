/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.legal.ibm.tags;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Expression.in;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import com.cloudant.client.internal.views.AllDocsRequestResponse;


import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.api.query.EmptyExpression;
import com.cloudant.client.api.query.ExecutionStats;
import com.cloudant.client.api.query.JsonIndex;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.Selector;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
/**
 * @author mbayser
 *
 */
@Repository
public class CloudantLegalTagRepository implements ILegalTagRepository {
	@Autowired
	private DpsHeaders headers;
	@Value("${ibm.legal.db.url}") 
	private String dbUrl;
	@Value("${ibm.legal.db.apikey:#{null}}")
	private String apiKey;
	@Value("${ibm.legal.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.legal.db.password:#{null}}")
	private String dbPassword;
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNamePrefix;
	
	@Value("${ibm.legal.db.credentials:#{null}}")
	private String credentialsJSON;
	
	@Value("${ibm.legal.db.name:legal-tags}")
	private String dbName;

	@Inject
	private ResourceLoader resourceLoader;
	
	private static final Logger logger = LoggerFactory.getLogger(CloudantLegalTagRepository.class);

	private IBMCloudantClientFactory cloudantFactory;
	private CloudantClient cloudant;
	private Database db = null;
	private static final int CLOUDANT_CONFLICT_RETRIES = 10;
	
	// When using this class as a Bean, there must be a default constructor
	// to create the object before the parameters can be injected. Only then
	// can the initialization happen
	public CloudantLegalTagRepository () {
	}
	
	// This constructor is meant to facilitate testing
	public CloudantLegalTagRepository(ServiceCredentials creds, String dbNamePrefix, String dataBaseName) throws MalformedURLException {
		doInit(creds, dbNamePrefix, dataBaseName);
	}
	
	@PostConstruct
	public void init() throws IOException {
		
		ServiceCredentials creds = null;
		if (dbUrl != null && apiKey != null) {
			creds = new ServiceCredentials(dbUrl, apiKey);
		} else if (dbUrl != null && dbUser != null) {
			creds = new ServiceCredentials(dbUrl, dbUser, dbPassword);
		} else {
			creds = new ServiceCredentials(new InputStreamReader(resourceLoader.getResource(credentialsJSON).getInputStream()));
		}
		
		doInit(creds, dbNamePrefix, dbName);
        
	}
	
	private void doInit(ServiceCredentials creds, String dbNamePrefix, String dataBaseName) throws MalformedURLException {
		cloudantFactory = new IBMCloudantClientFactory(creds);
		cloudantFactory.getGsonBuilder()
			.registerTypeAdapter(java.sql.Date.class,          CloudantBackedLegalTag.sqlDateSerializer)
			.registerTypeAdapter(java.sql.Date.class,          CloudantBackedLegalTag.sqlDateDeserializer)
			.registerTypeAdapter(LegalTag.class,               CloudantBackedLegalTag.serializer)
			.registerTypeAdapter(CloudantBackedLegalTag.class, CloudantBackedLegalTag.serializer)
			.registerTypeAdapter(CloudantBackedLegalTag.class, CloudantBackedLegalTag.deserializer);
		this.cloudant = cloudantFactory.getClient();
		this.db = cloudantFactory.getDatabase(cloudant, dbNamePrefix, dataBaseName);
		
		db.createIndex(JsonIndex.builder().name("is-valid-json-index").asc("is_valid").definition());
		db.createIndex(JsonIndex.builder().name("id-json-index").asc("_id").definition());

		try {
			LegalTag tag = new LegalTag();
			tag.setName("opendes-dps-integration-test-1566474656479");
			tag.setDescription("invalid date");
			tag.setIsValid(true);
			Properties tp = new Properties();
			tp.setCountryOfOrigin(Arrays.asList("US"));
			tp.setContractId("A1234");
			tp.setOriginator("MyCompany");
			tp.setDataType("Transferred Data");
			tp.setSecurityClassification("Public");
			tp.setPersonalData("No Personal Data");
			tp.setExportClassification("EAR99");
			tp.setExpirationDate(Date.valueOf("2005-12-11"));
			tag.setProperties(tp);
			tag.setDefaultId();
			db.save(tag);
			logger.info("Invalid tag created for integration tests.");
		} catch (DocumentConflictException e) {
			logger.info("Invalid Tag already exists.");
		}
		
		try {
			LegalTag tag = new LegalTag();
			tag.setName("opendes-public-usa-dataset-1");
			tag.setDescription("test for opendes-storage");
			tag.setIsValid(true);
			Properties tp = new Properties();
			tp.setCountryOfOrigin(Arrays.asList("US"));
			tp.setContractId(Properties.UNKNOWN_CONTRACT_ID);
			tp.setOriginator("MyCompany");
			tp.setDataType("Public Domain Data");
			tp.setSecurityClassification("Public");
			tp.setPersonalData("No Personal Data");
			tp.setExportClassification("EAR99");
			tp.setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);
			tag.setProperties(tp);
			tag.setDefaultId();
			db.save(tag);
			logger.info("Default tag created for integration tests.");
		} catch (DocumentConflictException e) {
			logger.info("Default Tag already exists.");
		}
		
	}

	@Override
	public Long create(LegalTag legalTag) {
		Long id = legalTag.getId();
		
		if (id == null) {
			throw new NullPointerException("Legal tag with null ID cannot be save to cloudant");
		}
		try {
			Response resp = db.save(legalTag);
			db.ensureFullCommit();
			logger.info("Query on {}", db.info().getDbName());
			if (200 <= resp.getStatusCode() && resp.getStatusCode() < 300) {
				
				if (resp.getStatusCode() == 202) {
					int retries = 3;
					// This test is crude and I think it can fail in two ways:
					// 1) Race condition with an update
					// 2) Sufficient quorum was still not achieved.
					// But it allows us to give a little more certainty
					while(retries-- > 0) {
						try {
							CloudantBackedLegalTag verify = retrieveExactlyOne(id);
							
							if (!verify.get_rev().equals(resp.getRev())) {						
								throw new AppException(409, "Cloudant sent a 202", "A LegalTag already exists for the given name");			
							}
							break;
						} catch (IllegalArgumentException ex) {}
					}
	 			}
				
				return id;
			} else {
				logger.error("Failed to save legal tag in cloudant. Status code: {}, Reason: {}", resp.getStatusCode(), resp.getReason());
				return null;
			}
		} catch(DocumentConflictException ex) {
			throw new AppException(409, ex.getReason(), "A LegalTag already exists for the given name");
		}
	}

	@Override
	public Collection<LegalTag> get(long[] ids) {
		// Would an "eq" be faster than an "in" if there was only one id?
		
		QueryBuilder builder = new QueryBuilder(in("_id", Arrays.stream(ids).mapToObj(l -> Long.toString(l)).toArray()));
		
		if (logger.isDebugEnabled()) {
			builder.executionStats(true);
		}
		
		QueryResult<CloudantBackedLegalTag> result = db.query(
    			builder.build()
    			, CloudantBackedLegalTag.class);
		
		logExecutionStat(db, result);
		
		if (result.getDocs().isEmpty()) {
			return new ArrayList<LegalTag>();
		} else {
			if (result.getDocs().size() > ids.length) {
				throw new IllegalStateException("Cardinality of Legal Tag result set is larger that the cardinality of IDs");
			}
			return Collections.unmodifiableList(result.getDocs());
		}
	}

	@Override
	public Boolean delete(LegalTag legalTag) {
		
		CloudantBackedLegalTag toDelete = null;

		if (legalTag instanceof CloudantBackedLegalTag && ((CloudantBackedLegalTag)legalTag).get_rev() != null) {
		//if (legalTag instanceof CloudantBackedLegalTag) {
			toDelete = (CloudantBackedLegalTag)legalTag;
		} else {
			try {
	    	toDelete = retrieveExactlyOne(legalTag.getId());
	    	copyNewValuesToOldVersion(legalTag, toDelete);
			} catch (IllegalArgumentException ex) {
				return false;
			}
		}
		
		int countDown = CLOUDANT_CONFLICT_RETRIES;
		
		while (countDown-- > 0) {
			try {
				Response resp = db.remove(toDelete.getId().toString(), toDelete.get_rev());
				if (200 <= resp.getStatusCode() && resp.getStatusCode() < 300) { 
					return true;
				} else {
					logger.error("Failed to delete legal tag in cloudant. Status code: {}, Reason: {}", resp.getStatusCode(), resp.getReason());
					return false;
				}
			} catch(DocumentConflictException ex) {
				toDelete = retrieveExactlyOne(legalTag.getId());
		    	copyNewValuesToOldVersion(legalTag, toDelete);
			}
		}
		
		throw new IllegalStateException("Cloudant delete had to be retried too many times due to update conflict");
	}

	private CloudantBackedLegalTag retrieveExactlyOne(Long id) {
		
		QueryBuilder builder = new QueryBuilder(eq("_id", id.toString()));
		
		if (logger.isDebugEnabled()) {
			builder.executionStats(true);
		}
		
		QueryResult<CloudantBackedLegalTag> result = db.query(builder.build()
    			, CloudantBackedLegalTag.class);
		
		logExecutionStat(db, result);
		
		long numResults = result.getDocs().size();
		
    	if (numResults != 1) {
    		if (numResults == 0) {
    			throw new IllegalArgumentException("Legal tag does not exist in the database");
    		} else {
    			throw new IllegalStateException("Legal tag is not unique");
    		}
    	}
    	return result.getDocs().get(0);
	}
	
	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.provider.interfaces.LegalTagRepository#update(org.opengroup.osdu.legal.tags.model.LegalTag)
	 */
	@Override
	public LegalTag update(LegalTag newLegalTag) {

		CloudantBackedLegalTag toUpdate = null;

		if (newLegalTag instanceof CloudantBackedLegalTag) {
			toUpdate = (CloudantBackedLegalTag)newLegalTag;
		} else {
			toUpdate = retrieveExactlyOne(newLegalTag.getId());
			copyNewValuesToOldVersion(newLegalTag, toUpdate);
		}

		int countDown = CLOUDANT_CONFLICT_RETRIES;

		while (countDown-- > 0) {
			try {
				Response resp = db.update(toUpdate);
				if (200 <= resp.getStatusCode() && resp.getStatusCode() < 300) { 
					return newLegalTag;
				} else {
					logger.error("Failed to save legal tag in cloudant. Status code: {}, Reason: {}", resp.getStatusCode(), resp.getReason());
					return null;
				}
			} catch(DocumentConflictException ex) {
				toUpdate = retrieveExactlyOne(newLegalTag.getId());
				copyNewValuesToOldVersion(newLegalTag, toUpdate);
			}
		}

		throw new IllegalStateException("Cloudant update had to be retried too many times due to update conflict");

	}
	
	private void copyNewValuesToOldVersion(LegalTag old, LegalTag _new) {
		assert(old.getId().equals(_new.getId()));
		old.setName(_new.getName());
		old.setDescription(_new.getDescription());
		old.setIsValid(_new.getIsValid());
		old.setProperties(_new.getProperties());
	}

	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.provider.interfaces.LegalTagRepository#list(org.opengroup.osdu.legal.tags.dataaccess.ListLegalTagArgs)
	 */
	@Override
	public Collection<LegalTag> list(ListLegalTagArgs args) {

		AllDocsRequestResponse allDocResponse = null;
		try {
			allDocResponse = (AllDocsRequestResponse) db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<LegalTag> legalTags;
		try {
			legalTags = allDocResponse.getResponse().getDocsAs(BanckendLegalTag.class)
					.stream()
					.filter(lTag ->  lTag.getIs_Valid() == args.getIsValid())
					.filter(f->f.getName().length() > headers.getPartitionId().length() && f.getName().substring(0,headers.getPartitionId().length()).equalsIgnoreCase(headers.getPartitionId()))
					.collect(Collectors.toList());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		logger.info("Query on {}", db.info().getDbName());
		//logExecutionStat(db, legalTags);


		return Collections.unmodifiableCollection(legalTags);
	}
	
	private void logExecutionStat(Database db, QueryResult<?> result) {
		final ExecutionStats stats = result.getExecutionStats();
		if (stats != null) {
			logger.debug("Query on {} returned {} results in {} ms. Total docs examined: {}. TotalKeys examined: {}. Warning message: {}", db.info().getDbName(), stats.getResultsReturned(), stats.getExecutionTimeMs(), stats.getTotalDocsExamined(), stats.getTotalKeysExamined(), result.getWarning());
		}
	}


}
