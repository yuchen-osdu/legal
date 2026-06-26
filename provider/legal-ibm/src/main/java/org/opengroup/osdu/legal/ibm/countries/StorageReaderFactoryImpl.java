/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/


package org.opengroup.osdu.legal.ibm.countries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.JsonIndex;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author mbayser
 *
 */
@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {
	
	private ConcurrentHashMap<String, IStorageReader> readers = new ConcurrentHashMap<>();
	
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
	
	@Value("${ibm.countries.db.name:countries}")
	private String dbName;
	
	@Inject
	private ResourceLoader resourceLoader;
	
	@Inject
	private JaxRsDpsLog logger;
	
	
	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.countries.StorageReaderFactory#getReader(org.opengroup.osdu.core.multitenancy.TenantInfo, java.lang.String)
	 */
	@Override
	
	public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
		
			ServiceCredentials creds = null;
			
			if (dbUrl != null && apiKey != null) {
				creds = new ServiceCredentials(dbUrl, apiKey);
			} else if (dbUrl != null && dbUser != null) {
				creds = new ServiceCredentials(dbUrl, dbUser, dbPassword);
			} else {
				try {
					creds = new ServiceCredentials(new InputStreamReader(resourceLoader.getResource(credentialsJSON).getInputStream()));
				} catch (IOException e) {
					logger.error(" 500, Malformed URL Invalid cloudant URL", e);
					throw new AppException(500, "Malformed URL", "Invalid cloudant URL", e);
				}
			} 
				
			IBMCloudantClientFactory cloudantFactory = new IBMCloudantClientFactory(creds);
			
			// Initialize data
			try {
				Database db = cloudantFactory.getDatabase(dbNamePrefix, dbName);
				
				System.out.println("creating indexes...");
				db.createIndex(JsonIndex.builder().name("tenant-region-json-index").asc("tenant", "region").definition());

				try {
					JsonObject json = new JsonObject();
					json.addProperty("_id", "integratio_test");
					json.addProperty("name", "Malaysia");
					json.addProperty("alpha2", "MY");
					json.addProperty("numeric", 458);
					json.addProperty("residencyRisk", "Client consent required");
					JsonArray array = new JsonArray();
					array.add("Transferred Data");
					json.add("typesNotApplyDataResidency", array);
					json.addProperty("tenant", "opendes");
					json.addProperty("region", "us");
					db.save(json);
					logger.info("MY Country created for integration tests.");
				} catch (DocumentConflictException e) {
					logger.info("MY Country already exists.");
				}
				
			} catch (MalformedURLException e1) {
				logger.error("Error initializing country database data.", e1);
			}

			return getReader(cloudantFactory, tenant, projectRegion, dbNamePrefix, dbName);		
	}
	
	public IStorageReader getReader(IBMCloudantClientFactory cloudantFactory, TenantInfo tenant, String projectRegion, String dbNamePrefix, String dbName) {

		final String key = tenant+":"+projectRegion;
		readers.computeIfAbsent(key, s -> {
			try {		
				return new StorageReaderImpl(tenant, projectRegion, cloudantFactory, dbNamePrefix, dbName);
			} catch (MalformedURLException | FileNotFoundException e) {
				logger.error("Error creating a Storage Reader", e);
				return null;
			}
		});
		return readers.get(key);		
	}
}