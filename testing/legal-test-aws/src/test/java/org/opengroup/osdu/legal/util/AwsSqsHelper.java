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

package org.opengroup.osdu.legal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opengroup.osdu.core.aws.v2.sqs.AmazonSQSConfig;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

public class AwsSqsHelper {
    private static final String REGION = "us-east-1";
    
    public static List<Message> getMessages() {
        String amazonSqsEndpoint = System.getenv("LEGAL_QUEUE");
        SqsClient sqsClient = new AmazonSQSConfig(REGION).AmazonSQS();
        
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(amazonSqsEndpoint)
                .build();
                
        ReceiveMessageResponse response = sqsClient.receiveMessage(receiveRequest);
        return response.messages();
    }

    public static void purgeQueue() {
        String amazonSqsEndpoint = System.getenv("LEGAL_QUEUE");
        SqsClient sqsClient = new AmazonSQSConfig(REGION).AmazonSQS();
        
        PurgeQueueRequest purgeRequest = PurgeQueueRequest.builder()
                .queueUrl(amazonSqsEndpoint)
                .build();
                
        sqsClient.purgeQueue(purgeRequest);
    }

    public static boolean checkLegalTagNameSent(Message message, String name) throws Exception {
        if (message == null)
            return false;
            
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(message.body());
        String data = root.path("Message").toString();
        
        // comes wrapped in non-escaped double quotes
        data = data.substring(1);
        data = data.substring(0, data.length() - 1);
        
        String dataCheck = "{\"statusChangedTags\":[{\"dataPartitionId\":\"" + TestUtils.getMyDataPartition() + "\",\"changedTagName\":\"" + name + "\",\"changedTagStatus\":\"incompliant\"}]}";
        dataCheck = dataCheck.replace("\"", "\\\\\"");
        
        return data.equals(dataCheck);
    }
}
