// Copyright 2020 IBM Corp. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.opengroup.osdu.core.ibm.messagebus.IMessageFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class IBMServiceBusHelper {

	// TODO should be reimplemented for AMQ
	/*
	public static boolean checkLegalTagNameSent(String message, String name) throws Exception {
        if(message == null)
            return false;
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonMessageElement = jsonParser.parse(message);
        JsonObject jsonMessage = jsonMessageElement.getAsJsonObject();        
        String data = jsonMessage.get("data").getAsString();
        return data.equals("{\"statusChangedTags\":[{\"changedTagName\":\"" + name + "\",\"changedTagStatus\":\"incompliant\"}]}");
    }

    public static String getMessage(int waitSeconds) throws Exception {
        
    	Thread.sleep(Duration.ofSeconds(waitSeconds).toMillis());
    	
    	String queueName = IBMLegalTagUtils.getQueueName() + "-" + IMessageFactory.LEGAL_QUEUE_NAME;
    	
    	Channel channel = null;
    	
		try {
			ConnectionFactory factory = new ConnectionFactory();
			
			ConnectionFactory connectionFactory = jmsTemplate.getConnectionFactory();
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			for (String queue : Arrays.asList(INDEXER_QUEUE_NAME, LEGAL_QUEUE_NAME, DEFAULT_QUEUE_NAME)) {
				session.createQueue(prefix + "-" + queue);
				logger.debug("Queue [" + prefix + "-" + queue + "] declared");
			}
			
			
			factory.setUri(IBMLegalTagUtils.getMessageQueueConnectionString());
			factory.setAutomaticRecoveryEnabled(true);
			Connection conn = factory.newConnection();
			channel = conn.createChannel();
			channel.queueDeclare(queueName, false, false, false, null);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TimeoutException e) {
			e.printStackTrace();
		}
    	String msg = null;
		try {
			boolean autoAck = false;
			GetResponse response = channel.basicGet(queueName, autoAck);
			if (response == null) {
			    // No message retrieved.
			} else {
			    //AMQP.BasicProperties props = response.getProps();
			    byte[] body = response.getBody();
			    long deliveryTag = response.getEnvelope().getDeliveryTag();
			    msg = new String(body);
			    System.out.println(" [x] Received '" + msg + "'");
			    channel.basicAck(deliveryTag, false); // acknowledge receipt of the message
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
		
    }
    */
}
