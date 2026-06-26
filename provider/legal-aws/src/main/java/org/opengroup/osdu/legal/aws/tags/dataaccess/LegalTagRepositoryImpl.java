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

package org.opengroup.osdu.legal.aws.tags.dataaccess;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.v2.dynamodb.interfaces.IDynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.v2.dynamodb.model.QueryPageResult;
import org.opengroup.osdu.core.aws.v2.dynamodb.util.RequestBuilderUtil.ScanRequestBuilder;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.InternalServerErrorException;

@ConditionalOnProperty(prefix = "repository", name = "implementation", havingValue = "dynamodb", matchIfMissing = true)
@Repository
@RequestScope
public class LegalTagRepositoryImpl implements ILegalTagRepository {

    private TenantInfo tenantInfo;
    private final IDynamoDBQueryHelperFactory queryHelperFactory;
    private final DpsHeaders headers;
    private final String legalRepositoryTableParameterRelativePath;
    private final JaxRsDpsLog log;

    @Autowired
    public LegalTagRepositoryImpl(IDynamoDBQueryHelperFactory queryHelperFactory,
            @Value("${aws.dynamodb.legalTable.ssm.relativePath}") String legalRepositoryTableParameterRelativePath,
            DpsHeaders headers, JaxRsDpsLog log) {
        this.queryHelperFactory = queryHelperFactory;
        this.headers = headers;
        this.legalRepositoryTableParameterRelativePath = legalRepositoryTableParameterRelativePath;
        this.log = log;
    }

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    private String getDataPartitionId() {
        if (this.tenantInfo == null) {
            log.warning("TenantInfo found to be null, defaulting to partition in headers");
            return headers.getPartitionIdWithFallbackToAccountId();
        }
        return tenantInfo.getDataPartitionId();
    }

    // Added a method to get a fresh query helper for each request
    private DynamoDBQueryHelper<LegalDoc> getCurrentQueryHelper() {
        String dataPartitionId = getDataPartitionId();
        return queryHelperFactory.createQueryHelper(dataPartitionId, legalRepositoryTableParameterRelativePath,
                LegalDoc.class);
    }

    @Override
    public Long create(LegalTag legalTag) {
        LegalDoc legalDoc = createLegalDocFromTag(legalTag);
        try {
            PutItemEnhancedRequest<LegalDoc> request = PutItemEnhancedRequest.builder(LegalDoc.class)
                    .item(legalDoc)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(Id)")
                            .build())
                    .build();

            return save(request);

        } catch (ConditionalCheckFailedException e) {
            throw new AppException(409, "Legal tag conflict",
                    String.format("A LegalTag already exists for the given name %s. Can't create again. Id is %s",
                            legalTag.getName(), legalTag.getId()));
        } catch (DynamoDbException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error parsing results",
                    e.getMessage());
        }
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        List<Pair<String, String>> compositeKeys = Arrays.stream(ids)
                .mapToObj(id -> Pair.of(String.valueOf(id), getDataPartitionId()))
                .toList();

        try {
            List<LegalDoc> docs = getCurrentQueryHelper().batchLoadByCompositePrimaryKey(compositeKeys);
            return docs.stream()
                    .map(this::createLegalTagFromDoc)
                    .toList();
        } catch (DynamoDbException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error parsing results", e.getMessage());
        }
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        try {
            getCurrentQueryHelper().deleteItem(createLegalDocFromTag(legalTag));
            return true;
        } catch (InternalServerErrorException e) {
            throw new AppException(HttpStatus.SC_SERVICE_UNAVAILABLE, "Service error occurred",
                    e.getMessage());
        } catch (DynamoDbException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error deleting tag",
                    e.getMessage());
        }
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        try {
            PutItemEnhancedRequest<LegalDoc> request = PutItemEnhancedRequest.builder(LegalDoc.class)
                    .item(createLegalDocFromTag(newLegalTag))
                    .conditionExpression(Expression.builder()
                            .expression("attribute_exists(Id)")
                            .build())
                    .build();

            save(request);

            return newLegalTag;
        } catch (ConditionalCheckFailedException e) {
            throw new AppException(409, "Legal tag not found",
                    String.format("A LegalTag with this Id %s does not exist",
                            newLegalTag.getId()));
        }
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        List<LegalDoc> docs;

        // Build the scan request with proper filtering
        ScanEnhancedRequest request = buildScanRequest(args);

        // Choose the appropriate scan method based on limit
        if (args.getLimit() <= 0) {
            docs = getCurrentQueryHelper().scanTable(request);
        } else {
            QueryPageResult<LegalDoc> pageResult = getCurrentQueryHelper().scanPage(request);
            docs = (pageResult != null) ? pageResult.getItems() : Collections.emptyList();
        }

        // No need to filter by isValid here since it's done in the database query
        return docs.stream()
                .map(this::createLegalTagFromDoc)
                .toList();
    }

    private ScanEnhancedRequest buildScanRequest(ListLegalTagArgs args) {
        // Create a filter expression that includes both dataPartitionId and isValid
        String filterExpression = "dataPartitionId = :partitionId AND IsValid = :isValid";

        // Set up the expression values
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":partitionId", AttributeValue.builder().s(getDataPartitionId()).build());
        expressionValues.put(":isValid", AttributeValue.builder().bool(args.getIsValid()).build());

        // Build the scan request
        ScanRequestBuilder<LegalDoc> builder = ScanRequestBuilder.forScan(LegalDoc.class)
                .filterExpression(filterExpression, expressionValues);

        // Add limit if positive
        if (args.getLimit() > 0) {
            builder.limit(args.getLimit());
        }

        // Handle cursor
        builder.cursor(args.getCursor() == null ? "" : args.getCursor());

        return builder.build();
    }

    private Long save(PutItemEnhancedRequest<LegalDoc> request) {
        LegalDoc legalDoc = request.item();
        if (legalDoc == null) {
            return -1L;
        }
        getCurrentQueryHelper().putItem(request);

        return Long.valueOf(legalDoc.getId());
    }

    private LegalTag createLegalTagFromDoc(LegalDoc ld) {
        LegalTag tag = new LegalTag();
        tag.setId(Long.valueOf(ld.getId()));
        tag.setName(ld.getName());
        tag.setIsValid(ld.getIsValid());
        tag.setDescription(ld.getDescription());
        tag.setProperties(ld.getProperties());
        return tag;
    }

    private LegalDoc createLegalDocFromTag(LegalTag legalTag) {
        LegalDoc legalDoc = new LegalDoc();
        legalDoc.setId(String.valueOf(legalTag.getId()));
        legalDoc.setDataPartitionId(getDataPartitionId());
        legalDoc.setDescription(legalTag.getDescription());
        legalDoc.setName(legalTag.getName());
        legalDoc.setProperties(legalTag.getProperties());
        legalDoc.setIsValid(legalTag.getIsValid());
        return legalDoc;
    }
}
