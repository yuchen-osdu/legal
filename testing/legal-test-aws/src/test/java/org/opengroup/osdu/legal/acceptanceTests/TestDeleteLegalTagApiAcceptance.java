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

package org.opengroup.osdu.legal.acceptanceTests;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opengroup.osdu.legal.util.Constants.DATA_PARTITION_ID;

import software.amazon.awssdk.services.sqs.model.Message;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.*;
import org.opengroup.osdu.legal.util.AwsLegalTagUtils;
import org.opengroup.osdu.legal.util.AwsSqsHelper;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDeleteLegalTagApiAcceptance extends DeleteLegalTagApiAcceptanceTests {

    @Before
    @Override
    public void setup() throws Exception  {
        this.legalTagUtils = new AwsLegalTagUtils();
        super.setup();
    }

    @After
    @Override
    public void teardown() throws Exception  {
        super.teardown();
        this.legalTagUtils = null;
    }

    @Test
    @Override
	public void should_return307_when_makingHttpRequest()throws Exception{
        //services are enforced to run in https on Azure
	}

    @Test
    @Override
	public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
        //services are enforced to run in https on Azure
	}
    
    @Test
    @Ignore
    // test is not stable
    public void should_receiveSubscriptionMessage_when_deletingAContractThatDoesExist() throws Exception {
        //clear out topic queue before test
        AwsSqsHelper.purgeQueue();

        name = LegalTagUtils.createRandomNameTenant();
        legalTagUtils.getResult(legalTagUtils.create(name), 201, String.class );
        validateAccess(204);

        boolean passed = false;
        int i = 0;
        while (i < 10 && !passed) {
            List<Message> messages = AwsSqsHelper.getMessages();
            if(messages.size() > 0)
                passed = AwsSqsHelper.checkLegalTagNameSent(messages.get(0), name);
            ++i;
            Thread.sleep(1000);
        }
        assertTrue("Pubsub message not received with tag: " + name, passed);
    }

    @Override
    protected ClientResponse validateAccess(int expectedResponse) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(DATA_PARTITION_ID, LegalTagUtils.getMyDataPartition());

        ClientResponse response = legalTagUtils.send(this.getApi(), this.getHttpMethod(), legalTagUtils.accessToken(), getBody(), getQuery(), headers);
        assertEquals(expectedResponse, response.getStatus());
        if(expectedResponse == 204)
            Assert.assertTrue(response.getType() == null || response.getLength() == 0 || response.getLength() == -1);
        else if(response.getType() != null) {
            Assert.assertTrue(response.getType().toString().toLowerCase().contains("application/json"));
        }
        return response;
    }


}
