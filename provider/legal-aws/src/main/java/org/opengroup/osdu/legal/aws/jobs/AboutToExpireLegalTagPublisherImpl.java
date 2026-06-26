package org.opengroup.osdu.legal.aws.jobs;

import java.util.List;

import org.opengroup.osdu.core.aws.v2.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTag;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class AboutToExpireLegalTagPublisherImpl implements IAboutToExpireLegalTagPublisher {

    private final SnsClient snsClient;
    private final String amazonSNSTopic;
    private final JaxRsDpsLog log;
    private final String osduAboutToExpireLegalTagTopic;

    public AboutToExpireLegalTagPublisherImpl(SnsClient snsClient,
                                              String amazonSNSTopic,
                                              @Value("${OSDU_ABOUT_TO_EXPIRE_LEGALTAG_TOPIC}") String osduAboutToExpireLegalTagTopic,
                                              JaxRsDpsLog log) {
        this.snsClient = snsClient;
        this.amazonSNSTopic = amazonSNSTopic;
        this.osduAboutToExpireLegalTagTopic = osduAboutToExpireLegalTagTopic;
        this.log = log;
    }

    @Override
    public void publish(String projectId, DpsHeaders headers, AboutToExpireLegalTags aboutToExpireLegalTags) {
        final int BATCH_SIZE = 50;
        // attributes
        PublishRequestBuilder<AwsAboutToExpireLegalTags> publishRequestBuilder = new PublishRequestBuilder<>();
        publishRequestBuilder.setGeneralParametersFromHeaders(headers);
        log.debug("Publishing to topic " + osduAboutToExpireLegalTagTopic);
        for (int i = 0; i < aboutToExpireLegalTags.getLegalTags().size(); i += BATCH_SIZE){
            List<AboutToExpireLegalTag> batch = aboutToExpireLegalTags.getLegalTags().subList(i, Math.min(aboutToExpireLegalTags.getLegalTags().size(), i + BATCH_SIZE));
            List<AwsAboutToExpireLegalTag> awsBatch = batch.stream()
                    .map(aboutToExpireLegalTag -> new AwsAboutToExpireLegalTag(aboutToExpireLegalTag.getTagName(), headers.getPartitionId(), aboutToExpireLegalTag.getExpirationDate()))
                    .toList();
            AwsAboutToExpireLegalTags awsBatchTags = new AwsAboutToExpireLegalTags(awsBatch);
            PublishRequest publishRequest = publishRequestBuilder.generatePublishRequest(osduAboutToExpireLegalTagTopic, amazonSNSTopic, awsBatchTags);
            snsClient.publish(publishRequest);
        }
    }
}

record AwsAboutToExpireLegalTags(List<AwsAboutToExpireLegalTag> aboutToExpireLegalTags) { }
