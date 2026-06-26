package org.opengroup.osdu.legal.acceptanceTests;

import com.google.pubsub.v1.PubsubMessage;
import org.junit.Ignore;
import org.opengroup.osdu.legal.util.GCPLegalTagUtils;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.PubSubHelper;
import org.opengroup.osdu.legal.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class TestDeleteLegalTagApiAcceptance extends DeleteLegalTagApiAcceptanceTests {

    @Before
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new GCPLegalTagUtils();
        super.setup();
    }

    @After
    @Override
    public void teardown() throws Exception {
        super.teardown();
        this.legalTagUtils = null;
    }

    @Test
    @Ignore
    // test is not stable 
    public void should_receiveSubscriptionMessage_when_deletingAContractThatDoesExist() throws Exception {
        String subscriptionName = "compliance-change--integration-test";

        //clear out topic queue before test
        while (PubSubHelper.pullMessage(TestUtils.getMyProjectAccountId(), subscriptionName, 20) != null) {}

        name = LegalTagUtils.createRandomNameTenant();
        this.legalTagUtils.getResult(legalTagUtils.create(name), 201, String.class );
        validateAccess(204);

        boolean passed = false;
        int i = 0;
        while (i < 10 && !passed) {
            PubsubMessage pubsubMessage = PubSubHelper.pullMessage(TestUtils.getMyProjectAccountId(), subscriptionName, 1);
            passed = (PubSubHelper.checkLegalTagNameSent(pubsubMessage, name));
            ++i;
            Thread.sleep(1000);
        }
        assertTrue("Pubsub message not received with tag: " + name, passed);
    }
}
