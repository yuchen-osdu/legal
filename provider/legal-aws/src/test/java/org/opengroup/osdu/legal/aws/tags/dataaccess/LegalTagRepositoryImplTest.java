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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.v2.dynamodb.interfaces.IDynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.v2.dynamodb.model.QueryPageResult;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

@ExtendWith(MockitoExtension.class)
class LegalTagRepositoryImplTest {
    private static final String DATA_PARTITION_ID = "data-partition-id";
    private static final String LEGAL_TABLE_PATH = "legal-table";

    @Mock
    private IDynamoDBQueryHelperFactory queryHelperFactory;

    @Mock
    private DynamoDBQueryHelper<LegalDoc> queryHelper;

    @Mock
    private DpsHeaders headers;

    @Mock
    private JaxRsDpsLog log;

    private LegalTagRepositoryImpl sut;

    @BeforeEach
    void setUp() {
        when(queryHelperFactory.createQueryHelper(
                DATA_PARTITION_ID,
                LEGAL_TABLE_PATH,
                LegalDoc.class)).thenReturn(queryHelper);
        sut = new LegalTagRepositoryImpl(queryHelperFactory, LEGAL_TABLE_PATH, headers, log);

        // Set up tenant info
        TenantInfo tenant = new TenantInfo();
        tenant.setDataPartitionId(DATA_PARTITION_ID);
        sut.setTenantInfo(tenant);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateLegalTagSuccessfully() {
        // Given
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);

        doNothing().when(queryHelper).putItem(any(PutItemEnhancedRequest.class));
        // When
        Long savedId = sut.create(legalTag);

        // Then
        assertEquals(id, savedId);
        verify(queryHelper).putItem(any(PutItemEnhancedRequest.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldThrowAppExceptionWhenCreatingDuplicateLegalTag() {
        // Given
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);

        doThrow(ConditionalCheckFailedException.class)
                .when(queryHelper)
                .putItem(any(PutItemEnhancedRequest.class));
        // When & Then
        AppException exception = assertThrows(AppException.class, () -> sut.create(legalTag));
        assertEquals(409, exception.getError().getCode());
    }

    @Test
    void shouldGetLegalTagsSuccessfully() {
        long[] ids = { 1234L, 5678L };
        List<LegalDoc> docs = List.of(
                createLegalDoc(ids[0], true),
                createLegalDoc(ids[1], true));

        // Stub queryHelper to return our dummy LegalDocs when called with any list.
        when(queryHelper.batchLoadByCompositePrimaryKey(any())).thenReturn(docs);

        // When
        Collection<LegalTag> results = sut.get(ids);

        // Then
        assertEquals(2, results.size());
        verify(queryHelper).batchLoadByCompositePrimaryKey(any());
    }

    @Test
    void shouldDeleteLegalTagSuccessfully() {
        // Given
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);

        doNothing().when(queryHelper).deleteItem(any(LegalDoc.class));

        // When
        Boolean result = sut.delete(legalTag);

        // Then
        assertTrue(result);
        verify(queryHelper).deleteItem(any(LegalDoc.class));
    }

    @Test
    void shouldThrowAppExceptionWhenDeleteFails() {
        // Given
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);

        doThrow(DynamoDbException.class)
                .when(queryHelper).deleteItem(any(LegalDoc.class));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> sut.delete(legalTag));
        assertEquals(500, exception.getError().getCode());
    }

    @Test
    void shouldListLegalTagsSuccessfully() {
        // Given
        String VALID_CURSOR_JSON = "{\"dummyKey\":{\"S\":\"dummyValue\"}}";
        String ENCODED_CURSOR = URLEncoder.encode(VALID_CURSOR_JSON, StandardCharsets.UTF_8);
        ListLegalTagArgs args = new ListLegalTagArgs();
        args.setLimit(2);
        args.setIsValid(true);
        args.setCursor(ENCODED_CURSOR);

        LegalDoc validDoc = createLegalDoc(1234L, true);

        List<LegalDoc> docs = List.of(validDoc);
        Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
        lastEvaluatedKey.put("id", AttributeValue.builder().s("lastId").build());

        QueryPageResult<LegalDoc> queryPageResult = new QueryPageResult<>(docs, lastEvaluatedKey, null);

        // Mock the database to return only the matching document and capture the scan
        // request
        when(queryHelper.scanPage(any(ScanEnhancedRequest.class)))
                .thenAnswer(invocation -> {
                    ScanEnhancedRequest scanRequest = invocation.getArgument(0);

                    // Get the filter expression from the scan request
                    Expression filterExpression = scanRequest.filterExpression();

                    // Verify that the filter expression contains both isValid and dataPartitionId conditions
                    String expressionString = filterExpression.expression();
                    Map<String, AttributeValue> expressionValues = filterExpression.expressionValues();

                    assertTrue(expressionString.contains("dataPartitionId = :partitionId"));
                    assertTrue(expressionString.contains("IsValid = :isValid"));

                    // Verify the expression values
                    assertTrue(expressionValues.containsKey(":partitionId"));
                    assertTrue(expressionValues.containsKey(":isValid"));
                    assertEquals(args.getIsValid(), expressionValues.get(":isValid").bool());
                    assertEquals(DATA_PARTITION_ID, expressionValues.get(":partitionId").s());

                    return queryPageResult;
                });

        // When
        Collection<LegalTag> results = sut.list(args);

        // Then
        assertEquals(1, results.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateLegalTagSuccessfully() {
        // Given
        long id = 1234;
        LegalTag legalTag = getLegalTagWithId(id);

        doNothing().when(queryHelper).putItem(any(PutItemEnhancedRequest.class));

        // When
        LegalTag result = sut.update(legalTag);

        // Then
        assertEquals(legalTag.getId(), result.getId());
        verify(queryHelper).putItem(any(PutItemEnhancedRequest.class));
    }

    private LegalTag getLegalTagWithId(long id) {
        LegalTag tag = new LegalTag();
        tag.setId(id);
        tag.setName("Test Tag");
        tag.setDescription("Test Description");
        return tag;
    }

    private LegalDoc createLegalDoc(long id, boolean isValid) {
        LegalDoc doc = new LegalDoc();
        doc.setId(String.valueOf(id));
        doc.setDataPartitionId(DATA_PARTITION_ID);
        doc.setName("Test Tag");
        doc.setDescription("Test Description");
        doc.setIsValid(isValid);
        return doc;
    }
}
