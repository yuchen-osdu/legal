package org.opengroup.osdu.legal.util;

import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.*;

import java.util.ArrayList;
import java.util.List;

public abstract class PubSubHelper {

    public static boolean checkLegalTagNameSent(PubsubMessage pubsubMessage, String name) {
        if(pubsubMessage == null)
            return false;
        String msg = pubsubMessage.getData().toStringUtf8();
        System.out.println(msg);
        return msg.equals("{\"statusChangedTags\":[{\"changedTagName\":\"" + name + "\",\"changedTagStatus\":\"incompliant\"}]}");
    }
    public static PubsubMessage pullMessage(String projectId, String subscriptionId, int msgsToPull) throws Exception{
        SubscriberStubSettings subscriberStubSettings =
                SubscriberStubSettings.newBuilder().build();
        try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
            String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
            PullRequest pullRequest =
                    PullRequest.newBuilder()
                            .setMaxMessages(msgsToPull)
                            .setReturnImmediately(true) // return immediately if messages are not available
                            .setSubscription(subscriptionName)
                            .build();

            // use pullCallable().futureCall to asynchronously perform this operation
            PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
            List<String> ackIds = new ArrayList<>();
            for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
                System.out.println("Message received: " + message.getMessage().toString());
                ackIds.add(message.getAckId());
            }
            if(ackIds.size() == 0)
                return null;

            // acknowledge received messages
            AcknowledgeRequest acknowledgeRequest =
                    AcknowledgeRequest.newBuilder()
                            .setSubscription(subscriptionName)
                            .addAllAckIds(ackIds)
                            .build();

            subscriber.acknowledgeCallable().call(acknowledgeRequest);
            List<ReceivedMessage> msgs = pullResponse.getReceivedMessagesList();
            if(msgs.size() > 0){
                return msgs.get(0).getMessage();
            }else{
                return null;
            }
        }
    }
}
