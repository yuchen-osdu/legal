/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.legal.util;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.opengroup.osdu.core.aws.v2.cognito.AWSCognitoClient;
import org.opengroup.osdu.core.aws.v2.configurationsetup.ConfigSetup;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBConfig;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.v2.s3.S3Config;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AwsLegalTagUtils extends LegalTagUtils {
    private static final String FILE_NAME = "Legal_COO.json";
    private static final String BUCKET_NAME_AWS = System.getProperty("S3_LEGAL_CONFIG_BUCKET", System.getenv("S3_LEGAL_CONFIG_BUCKET"));

    private static final String TABLE_PREFIX = "TABLE_PREFIX";
    private static final String DYNAMO_DB_REGION = "DYNAMO_DB_REGION";
    private static final String DYNAMO_DB_ENDPOINT = "DYNAMO_DB_ENDPOINT";


    private String bearerToken = "";

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        String amazonS3Endpoint = System.getProperty("AWS_S3_ENDPOINT", System.getenv("AWS_S3_ENDPOINT"));
        String amazonS3Region = System.getProperty("AWS_S3_REGION", System.getenv("AWS_S3_REGION"));
        String dataPartitionId = System.getProperty("MY_TENANT", System.getenv("MY_TENANT"));

        S3Config s3Config = new S3Config(amazonS3Endpoint, amazonS3Region);
        S3Client s3Client = s3Config.amazonS3();

        try {
            String key = String.format("%s/%s", dataPartitionId, FILE_NAME);
            byte[] fileContent = readTestFile("TenantConfigTestingPurpose.json").getBytes();
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(BUCKET_NAME_AWS)
                .key(key)
                .build();
                
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized String accessToken() throws Exception {
        if (bearerToken.equals("")) {
            // Using the no-arg constructor which will get values from environment variables
            AWSCognitoClient client = new AWSCognitoClient();
            bearerToken = client.getToken();
        }
        return "Bearer " + bearerToken;
    }

    @Override
    public List<String> readCOOCountries(String storageAccount, String defaultCOOFileName) throws IOException {
        return List.of();
    }

    public void insertExpiredLegalTag() {
        // directly create expired legal tag document
        String integrationTagTestName = String.format("%s-dps-integration-test-1566474656479", getMyDataPartition()); // name has to match what's hardcoded in the test
        LegalDoc doc = new LegalDoc();
        doc.setDescription("Expired integration test tag");
        doc.setName(integrationTagTestName);
        doc.setId(Integer.toString(integrationTagTestName.hashCode()));
        doc.setProperties(getLegalTagProperties());
        doc.setDataPartitionId(getMyDataPartition());

        String dynamoDbRegion = System.getenv(DYNAMO_DB_REGION);
        String dynamoDbEndpoint = System.getenv(DYNAMO_DB_ENDPOINT);
        String tableName = String.format("%s-shared-LegalRepository", System.getenv(TABLE_PREFIX));

        // Create DynamoDB config
        DynamoDBConfig dynamoDBConfig = DynamoDBConfig.builder()
            .region(dynamoDbRegion)
            .endpoint(dynamoDbEndpoint)
            .configSetup(ConfigSetup.setUpConfig())
            .build();

        // Create enhanced client
        DynamoDbEnhancedClient enhancedClient = dynamoDBConfig.dynamoDbEnhancedClient();
        
        // Create table mapping
        TableSchema<LegalDoc> schema = TableSchema.fromBean(LegalDoc.class);
        DynamoDbTable<LegalDoc> table = enhancedClient.table(tableName, schema);
        
        // Create query helper
        DynamoDBQueryHelper<LegalDoc> queryHelper = DynamoDBQueryHelper.<LegalDoc>builder()
            .client(enhancedClient)
            .table(table)
            .itemType(LegalDoc.class)
            .build();

        // Check if legal tag exists and delete it
        queryHelper.getItem(doc.getId(), doc.getDataPartitionId())
            .ifPresent(existingDoc -> queryHelper.deleteItem(doc.getId(), doc.getDataPartitionId()));

        // Save the new document
        queryHelper.putItem(doc);
    }

    private org.opengroup.osdu.core.common.model.legal.Properties getLegalTagProperties(){
        org.opengroup.osdu.core.common.model.legal.Properties properties = new org.opengroup.osdu.core.common.model.legal.Properties();
        List<String> countryOfOrigin = new ArrayList<>();
        Date date = new Date(1234567898765L);
        countryOfOrigin.add("US");
        properties.setCountryOfOrigin(countryOfOrigin);
        properties.setContractId("A1234");
        properties.setExpirationDate(date);
        properties.setOriginator("MyCompany");
        properties.setDataType("Transferred Data");
        properties.setSecurityClassification("Public");
        properties.setPersonalData("No Personal Data");
        properties.setExportClassification("EAR99");
        return properties;
    }
}
