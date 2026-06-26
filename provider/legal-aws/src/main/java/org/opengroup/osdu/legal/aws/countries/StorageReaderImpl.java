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

import org.opengroup.osdu.core.aws.v2.s3.IS3ClientFactory;
import org.opengroup.osdu.core.aws.v2.s3.S3ClientWithBucket;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class StorageReaderImpl implements IStorageReader {

    private final DpsHeaders dpsHeaders;
    private final IS3ClientFactory s3ClientFactory;
    private final String s3ConfigBucketParameterRelativePath;
    private final String legalConfigFileName;

    @Autowired
    public StorageReaderImpl(DpsHeaders dpsHeaders,
            IS3ClientFactory s3ClientFactory,
            @Value("${aws.s3.configbucket.ssm.relativePath}") String s3ConfigBucketParameterRelativePath,
            @Value("${aws.s3.legal.config.file-name}") String legalConfigFileName) {
        this.dpsHeaders = dpsHeaders;
        this.s3ClientFactory = s3ClientFactory;
        this.s3ConfigBucketParameterRelativePath = s3ConfigBucketParameterRelativePath;
        this.legalConfigFileName = legalConfigFileName;
    }

    @Override
    public byte[] readAllBytes() {
        return getConfigFile().getBytes(); // should return a json format of an array of Country class
    }

    public String getConfigFile() {
        S3ClientWithBucket s3ClientWithBucket = s3ClientFactory.getS3ClientForPartition(
                dpsHeaders.getPartitionIdWithFallbackToAccountId(), s3ConfigBucketParameterRelativePath);
        S3Client s3 = s3ClientWithBucket.getS3Client();
        String legalConfigBucketName = s3ClientWithBucket.getBucketName();
        String key = String.format("%s/%s", dpsHeaders.getPartitionId(), legalConfigFileName);
        String contents;

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(legalConfigBucketName)
                    .key(key)
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(getObjectRequest);
            contents = objectBytes.asUtf8String();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                contents = createDefaultConfigFile(s3, legalConfigBucketName, key);
            } else {
                throw new AppException(500, "Amazon S3 exception getting config file", e.getMessage());
            }
        }
        return contents;
    }

    private String createDefaultConfigFile(S3Client s3, String legalConfigBucketName, String key) {
        String defaultContents = "[]";
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(legalConfigBucketName)
                    .key(key)
                    .build();
            s3.putObject(putObjectRequest, RequestBody.fromString(defaultContents));
        } catch (S3Exception e) {
            throw new AppException(500, "Exception creating default config file", e.getMessage());
        }
        return defaultContents;
    }
}
