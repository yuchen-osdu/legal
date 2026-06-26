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

package org.opengroup.osdu.legal.aws.countries;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.s3.IS3ClientFactory;
import org.opengroup.osdu.core.aws.v2.s3.S3ClientWithBucket;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class StorageReaderImplTest {

    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    private IS3ClientFactory s3ClientFactory;

    @Mock
    private S3ClientWithBucket s3ClientWithBucket;

    @Mock
    private ResponseBytes<GetObjectResponse> responseBytes;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private StorageReaderImpl storageReaderImpl;

    // common test data
    private final String partitionId = "test-partition";
    private final String bucketName = "test-bucket";
    private final String legalConfigFileName = "legal-config.json";
    private final String key = String.format("%s/%s", partitionId, legalConfigFileName);

    @BeforeEach
    void setUp() {
        when(dpsHeaders.getPartitionId()).thenReturn(partitionId);
        when(dpsHeaders.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionId);

        ReflectionTestUtils.setField(storageReaderImpl, "s3ConfigBucketParameterRelativePath", bucketName);
        ReflectionTestUtils.setField(storageReaderImpl, "legalConfigFileName", legalConfigFileName);

        when(s3ClientFactory.getS3ClientForPartition(partitionId, bucketName))
                .thenReturn(s3ClientWithBucket);
        when(s3ClientWithBucket.getS3Client()).thenReturn(s3Client);
        when(s3ClientWithBucket.getBucketName()).thenReturn(bucketName);
    }

    @Test
    void testGetConfigFile_HappyPath() {
        String expectedContent = "{\"countries\":[]}";
        when(responseBytes.asUtf8String()).thenReturn(expectedContent);
        when(s3Client.getObjectAsBytes((GetObjectRequest) any())).thenReturn(responseBytes);

        String result = storageReaderImpl.getConfigFile();

        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObjectAsBytes(captor.capture());
        GetObjectRequest capturedRequest = captor.getValue();

        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());
        assertEquals(expectedContent, result);
    }

    @Test
    void testReadAllBytes() {
        String expectedContent = "{\"countries\":[]}";
        when(s3Client.getObjectAsBytes((GetObjectRequest) any())).thenReturn(responseBytes);
        when(responseBytes.asUtf8String()).thenReturn(expectedContent);

        byte[] resultBytes = storageReaderImpl.readAllBytes();
        assertArrayEquals(expectedContent.getBytes(StandardCharsets.UTF_8), resultBytes);
    }

    @Test
    void testGetConfigFile_NotFound_UsesDefault() {
        AwsServiceException notFoundException = S3Exception.builder().statusCode(404).message("Not Found").build();
        when(s3Client.getObjectAsBytes((GetObjectRequest) any())).thenThrow(notFoundException);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String result = storageReaderImpl.getConfigFile();
        assertEquals("[]", result);
    }

    @Test
    void testGetConfigFile_ServerError_ThrowsAppException() {
        AwsServiceException serverError = S3Exception.builder()
                .statusCode(500)
                .message("Internal Server Error")
                .build();
        when(s3Client.getObjectAsBytes((GetObjectRequest) any())).thenThrow(serverError);

        AppException ex = assertThrows(AppException.class, () -> storageReaderImpl.getConfigFile());
        assertTrue(ex.getMessage().contains("Internal Server Error"));
    }
}
