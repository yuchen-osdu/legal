package org.opengroup.osdu.legal.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import org.opengroup.osdu.core.common.model.info.VersionInfo;

public class VersionInfoUtils {

  public VersionInfo getVersionInfoFromResponse(ClientResponse response) {
    assertTrue(response.getType().toString().contains("application/json"));
    String json = response.getEntity(String.class);
    Gson gson = new Gson();
    return gson.fromJson(json, VersionInfo.class);
  }

}
