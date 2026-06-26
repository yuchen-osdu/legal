package org.opengroup.osdu.legal.acceptanceTests;

import org.opengroup.osdu.legal.util.GCPLegalTagUtils;

import org.junit.After;
import org.junit.Before;

public class TestGetLegalTagApiAcceptance extends GetLegalTagApiAcceptanceTests {

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

}
