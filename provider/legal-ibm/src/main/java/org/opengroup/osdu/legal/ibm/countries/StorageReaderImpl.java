/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.legal.ibm.countries;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Operation.and;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.JsonParser;
import jakarta.inject.Inject;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * @author mbayser
 *
 */
public class StorageReaderImpl implements IStorageReader {
	
    private final TenantInfo tenantInfo;
    private final String cloudRegion;
    private final String dbNamePrefix;
    private final String dbName;
    private final IBMCloudantClientFactory cloudantFactory;
    private final CloudantClient cloudant;
    private static final long CACHE_EXPIRATION_MS = 60000;
    private AtomicLong lastUpdate = new AtomicLong(0);
    private WeakReference<byte[]> cache = new WeakReference<byte[]>(null);
	private static final String LEGAL_CONFIG_FILE_NAME = "legal.coo.json";

    public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion, IBMCloudantClientFactory cloudantFactory, String dbNamePrefix, String dbName) throws MalformedURLException, FileNotFoundException {
        this.tenantInfo = tenantInfo;
        this.cloudRegion = projectRegion;
        this.dbNamePrefix =  dbNamePrefix;
        this.dbName = dbName;
        this.cloudantFactory = cloudantFactory;
        this.cloudant = cloudantFactory.getClient();
    }
    
    @Inject
	private JaxRsDpsLog logger;

	/* (non-Javadoc)
	 * @see org.opengroup.osdu.legal.countries.StorageReader#readAllBytes()
	 */
	@Override
	public byte[] readAllBytes() {
		 //There is a race condition here, but other than a few wasteful HTTP requests it does no harm.
		// A mutex here on the other hand could lead to increased latency.
		if ((lastUpdate.get() + CACHE_EXPIRATION_MS) > System.currentTimeMillis()) {
			byte[] c = cache.get();
			if (c != null) {
				return c;
			}
		}
		
		try {
			Database db = cloudantFactory.getDatabase(cloudant, dbNamePrefix, dbName);

			QueryResult<JsonObject> result = db.query(new QueryBuilder(and(eq("tenant", tenantInfo.getName()), eq("region", cloudRegion)))
					.fields("name", "alpha2", "numeric", "residencyRisk", "typesNotApplyDataResidency").build(), JsonObject.class);
			JsonArray array = new JsonArray();
			for (JsonObject s: result.getDocs()) {
				array.add(s);
			}
			String content = new String(Files.readAllBytes(Paths.get("/config/"+LEGAL_CONFIG_FILE_NAME)));
			JsonArray data = JsonParser.parseString(content).getAsJsonArray();

			array.forEach(data::add);
			byte[] blob = cloudant.getGson().toJson(data).getBytes();
	        cache = new WeakReference<byte[]>(blob);
	        lastUpdate.set(System.currentTimeMillis());
	        
	        return blob;
		} catch (IOException e) {
		    logger.error(" 500, IOException", e);
			throw new AppException(500, "IOException", "Invalid cloudant URL", e);
		}
	}

}
