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
import java.util.stream.Collectors;

import org.opengroup.osdu.core.aws.v2.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class LegalTagPublisherImpl implements ILegalTagPublisher {

    private final SnsClient snsClient;
    private final String amazonSNSTopic;
    private final JaxRsDpsLog log;
    private final String osduLegalTopic;

    public LegalTagPublisherImpl(SnsClient snsClient,
            String amazonSNSTopic,
            @Value("${OSDU_TOPIC}") String osduLegalTopic,
            JaxRsDpsLog log) {
        this.snsClient = snsClient;
        this.amazonSNSTopic = amazonSNSTopic;
        this.osduLegalTopic = osduLegalTopic;
        this.log = log;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) {
        final int BATCH_SIZE = 50;
        // attributes
        PublishRequestBuilder<AwsStatusChangedTags> publishRequestBuilder = new PublishRequestBuilder<>();
        publishRequestBuilder.setGeneralParametersFromHeaders(headers);
        log.debug("Publishing to topic " + osduLegalTopic);
        for (int i = 0; i < tags.getStatusChangedTags().size(); i += BATCH_SIZE) {
            List<StatusChangedTag> batch = tags.getStatusChangedTags().subList(i,
                    Math.min(tags.getStatusChangedTags().size(), i + BATCH_SIZE));
            List<AwsStatusChangedTag> awsBatch = batch.stream()
                    .map(t -> new AwsStatusChangedTag(t.getChangedTagName(), t.getChangedTagStatus(),
                            headers.getPartitionId()))
                    .collect(Collectors.toList());
            AwsStatusChangedTags awsBatchTags = new AwsStatusChangedTags(awsBatch);
            PublishRequest publishRequest = publishRequestBuilder.generatePublishRequest(osduLegalTopic, amazonSNSTopic,
                    awsBatchTags);
            snsClient.publish(publishRequest);
        }
    }
}

record AwsStatusChangedTags(List<AwsStatusChangedTag> statusChangedTags) { }

