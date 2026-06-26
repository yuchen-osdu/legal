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

package org.opengroup.osdu.legal.aws.cache;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;

@TestPropertySource(properties = {"aws.elasticache.cluster.endpoint=testHost", "aws.elasticache.cluster.port=1234", "aws.elasticache.cluster.key=testKey"})
class GroupCacheTest {
    
    @Mock
    private K8sLocalParameterProvider provider;
    @Mock
    private DpsHeaders headers;

    private final String dataPartitionId = "testPartitionId";

    private final String authorization = "testAuthorization";

    private static final String REDIS_SEARCH_HOST = "testHost";
    private static final String REDIS_SEARCH_PORT = "1234";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(provider.getParameterAsStringOrDefault("CACHE_CLUSTER_ENDPOINT", null)).thenReturn(REDIS_SEARCH_HOST);
        when(provider.getParameterAsStringOrDefault("CACHE_CLUSTER_PORT", null)).thenReturn(REDIS_SEARCH_PORT);
        when(provider.getLocalMode()).thenReturn(true);
        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
        when(headers.getAuthorization()).thenReturn(authorization);
    }

    @Test
    void should_createGroupCache_withExpectedArguments() throws Throwable {
        assertDoesNotThrow(() -> (new GroupCache<String, Groups>()));
    }
}