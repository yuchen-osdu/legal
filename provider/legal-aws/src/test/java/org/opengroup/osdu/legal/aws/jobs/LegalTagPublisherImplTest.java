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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.v2.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ExtendWith(MockitoExtension.class)
class LegalTagPublisherImplTest {

    private LegalTagPublisherImpl legalTagPublisherImpl;

    @Mock
    private SnsClient snsClient;

    @Mock
    private DpsHeaders headers;

    @Mock
    private StatusChangedTags tags;

    @Mock
    private StatusChangedTag tag;

    @Mock
    private JaxRsDpsLog log;

    private final String amazonSNSTopic = "amazon-sns-topic";
    private final String osduLegalTopic = "osdu-legal-topic";

    @BeforeEach
    void setup() {
        legalTagPublisherImpl = new LegalTagPublisherImpl(snsClient, amazonSNSTopic, osduLegalTopic, log);
    }

    @Test
    void testPublish() {
        when(headers.getPartitionId()).thenReturn("test-partition");
        when(tags.getStatusChangedTags()).thenReturn(List.of(tag));

        try (MockedConstruction<PublishRequestBuilder> mockedBuilder = mockConstruction(PublishRequestBuilder.class,
                (mock, context) -> {
                    when(mock.generatePublishRequest(eq(osduLegalTopic), eq(amazonSNSTopic),
                            any(AwsStatusChangedTags.class)))
                            .thenReturn(PublishRequest.builder().topicArn(osduLegalTopic).build());
                })) {

            legalTagPublisherImpl.publish("projectId", headers, tags);
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

    }
}
