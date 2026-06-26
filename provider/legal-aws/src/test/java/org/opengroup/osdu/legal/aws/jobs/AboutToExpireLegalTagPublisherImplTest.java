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

package org.opengroup.osdu.legal.aws.jobs;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.v2.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTag;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;


@ExtendWith(MockitoExtension.class)
class AboutToExpireLegalTagPublisherImplTest {
    
    @InjectMocks
    private AboutToExpireLegalTagPublisherImpl aboutToExpireLegalTagPublisherImpl;

    @Mock
    private AmazonSNSConfig snsConfig;

    @Mock
    private K8sLocalParameterProvider k8sLocalParameterProvider;

    @Mock
    private SnsClient snsClient;

    @Mock
    private DpsHeaders headers;

    @Mock
    private PublishRequestBuilder<AwsAboutToExpireLegalTags> publishRequestBuilder;

    @Mock
    private AboutToExpireLegalTags tags;

    @Mock
    private AboutToExpireLegalTag tag;

    @Mock
    private JaxRsDpsLog log;


    @Test
    void testPublish() {
        List<AboutToExpireLegalTag> tagList = List.of(tag);
        when(tags.getLegalTags()).thenReturn(tagList);                                                                           
        aboutToExpireLegalTagPublisherImpl.publish("projectId", headers, tags);
        
        verify(snsClient, times(1)).publish(any(PublishRequest.class));

    }       

}

