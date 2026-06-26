package org.opengroup.osdu.legal.acceptanceTests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.util.IBMLegalTagUtils;

public class TestGetInfoApiAcceptance extends GetInfoApiAcceptanceTests {

  @Before
  @Override
  public void setup() throws Exception {
    this.legalTagUtils = new IBMLegalTagUtils();
    super.setup();
  }

  @After
  @Override
  public void teardown() throws Exception {
    super.teardown();
    this.legalTagUtils = null;
  }

  @Test
  @Override
  public void should_return307_when_makingHttpRequest() {
    // services are enforced to run in https on OpenShift
  }

  @Test
  @Override
  public void should_return401_when_makingHttpRequestWithoutToken() {
    // services are enforced to run in https on OpenShift
  }
}
