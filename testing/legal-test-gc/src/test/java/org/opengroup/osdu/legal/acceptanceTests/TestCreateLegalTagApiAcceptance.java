package org.opengroup.osdu.legal.acceptanceTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.util.GCPLegalTagUtils;

public class TestCreateLegalTagApiAcceptance extends CreateLegalTagApiAcceptanceTests {

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

  @Override
  @Test
  public void should_onlyLetAMaximumOf1LegaltagBeCreated_when_tryingToCreateMultipleVersionsOfTheSameContractAtTheSameTime()
      throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<Callable<ClientResponse>> tasks = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      Callable<ClientResponse> task = () -> {
        try {
          return legalTagUtils.create(name);
        } catch (Exception ex) {
          return null;
        }
      };
      tasks.add(task);
    }

    List<Future<ClientResponse>> responses = executor.invokeAll(tasks);
    executor.shutdown();
    executor.awaitTermination(20, TimeUnit.SECONDS);

    int sucessResponseCount = 0;
    int non409ErrorResponseCount = 0;
    for (Future<ClientResponse> future : responses) {
      if (future.get().getStatus() == 201) {
        sucessResponseCount++;
      } else if (future.get().getStatus() != 409) {
        non409ErrorResponseCount++;
      }
    }

    assertTrue("Expected 1 successful response. Actual " + sucessResponseCount,
        sucessResponseCount <= 1);
    assertEquals(0, non409ErrorResponseCount);
  }


}
