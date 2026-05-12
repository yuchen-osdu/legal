package org.opengroup.osdu.legal.acceptancetests;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.info.FeatureFlagStateResolver;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.opengroup.osdu.legal.util.AcceptanceBaseTest;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.VersionInfoUtils;

import com.sun.jersey.api.client.ClientResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public final class GetInfoApiAcceptanceTests extends AcceptanceBaseTest {

  private static final VersionInfoUtils VERSION_INFO_UTILS = new VersionInfoUtils();

  // Feature flag property constant - matches the value used in service configuration
  private static final String EXPOSE_FEATUREFLAG_ENABLED_PROPERTY = "expose_featureflag.enabled";


  // Expected feature flags for legal service
  private static final String[] expectedFeatureFlags = {
      "featureFlag.aboutToExpireLegalTag.enabled",
      "featureFlag.legalTagQueryApi.enabled",
      "featureFlag.legalTagQueryApiFreeTextAllFields.enabled"
  };


  @BeforeEach
  @Override
  public void setup() throws Exception {
      this.legalTagUtils = new LegalTagUtils();
      super.setup();
  }

  @AfterEach
  @Override
  public void teardown() throws Exception {
      super.teardown();
      this.legalTagUtils = null;
  }

  @Test
  public void should_returnInfo() throws Exception {
      ClientResponse response = send(StringUtils.EMPTY, HttpStatus.SC_OK);
      VersionInfo responseObject = VERSION_INFO_UTILS.getVersionInfoFromResponse(response);

      assertNotNull(responseObject.getGroupId());
      assertNotNull(responseObject.getArtifactId());
      assertNotNull(responseObject.getVersion());
      assertNotNull(responseObject.getBuildTime());
      assertNotNull(responseObject.getBranch());
      assertNotNull(responseObject.getCommitMessage());
      assertNotNull(responseObject.getCommitId());

      List<FeatureFlagStateResolver.FeatureFlagState> featureFlagStates = responseObject.getFeatureFlagStates();

      boolean isFeatureFlagExposureEnabled = isFeatureFlagExposureEnabled();

      if (!isFeatureFlagExposureEnabled) {
          assertNull(featureFlagStates);
          return;
      }

      assertNotNull(featureFlagStates);
      assertFalse(featureFlagStates.isEmpty());

      for (String expectedFeatureFlag : expectedFeatureFlags) {
          assertTrue(
              featureFlagStates.stream()
                  .anyMatch(state -> expectedFeatureFlag.equals(state.getName())),
              "Expected feature flag not found: " + expectedFeatureFlag
          );
      }
  }

  private boolean isFeatureFlagExposureEnabled() {
      String propertyValue = Optional.ofNullable(System.getProperty(EXPOSE_FEATUREFLAG_ENABLED_PROPERTY))
          .orElseGet(() -> System.getenv("EXPOSE_FEATUREFLAG_ENABLED"));
      return !"false".equalsIgnoreCase(propertyValue);
  }

  @Override
  protected String getApi() {
    return "info";
  }

  @Override
  protected String getHttpMethod() {
    return "GET";
  }

  @Override
  public void should_return307_when_makingHttpRequest() {
    // not actual for this endpoint
  }

  @Override
  public void should_return401_when_makingHttpRequestWithoutToken() {
    // not actual for this endpoint
  }
}
