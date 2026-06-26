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

package org.opengroup.osdu.legal.acceptanceTests;

import static junit.framework.TestCase.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.util.IBMLegalTagUtils;
import org.opengroup.osdu.legal.util.IBMServiceBusHelper;
import org.opengroup.osdu.legal.util.LegalTagUtils;

public class TestDeleteLegalTagApiAcceptance extends DeleteLegalTagApiAcceptanceTests {

    @Before
    @Override
    public void setup() throws Exception  {
        this.legalTagUtils = new IBMLegalTagUtils();
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
        //services are enforced to run in https on OpenShift
	}

    @Test
    @Override
	public void should_return401_when_makingHttpRequestWithoutToken()throws Exception{
        //services are enforced to run in https on OpenShift
	}
    
    /*@Test
    public void should_receiveSubscriptionMessage_when_deletingAContractThatDoesExist() throws Exception {

        //clear out topic queue before test
        while (IBMServiceBusHelper.getMessage(1) != null) {}

        name = LegalTagUtils.createRandomNameTenant();
        legalTagUtils.getResult(legalTagUtils.create(name), 201, String.class );
        validateAccess(204);

        boolean passed = false;
        int i = 0;
        while (i < 10 && !passed) {
            String message = IBMServiceBusHelper.getMessage(2);
            System.out.println(">>>> " + message);
            passed = IBMServiceBusHelper.checkLegalTagNameSent(message, name);
            ++i;
            Thread.sleep(1000);
        }
        assertTrue("Pubsub message not received with tag: " + name, passed);
    }*/
}
