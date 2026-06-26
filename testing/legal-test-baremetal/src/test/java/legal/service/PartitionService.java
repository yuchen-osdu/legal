/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package legal.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import legal.util.CustomHttpClientResponseHandler;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.Assert;
import org.opengroup.osdu.core.common.partition.Property;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
// TODO: This Class is using httpClient 5 instead of 4. Need to rewrite core int tests.
public class PartitionService {

  private static final Gson gson = new Gson();

  public static String getPartitionProperty(String property) throws Exception {
    String partitionApi = System.getProperty("PARTITION_API", System.getenv("PARTITION_API"));
    CloseableHttpResponse response =
        send(
            partitionApi,
            "/partitions/" + LegalTagUtils.getMyProjectAccountId(),
            "GET",
            new HashMap<>(),
            null);
    InputStream content = response.getEntity().getContent();

    Type parametrizedType =
        TypeToken.getParameterized(Map.class, new Class[] {String.class, Property.class}).getType();
    Map<String, Property> properties =
        gson.fromJson(new InputStreamReader(content), parametrizedType);

    if (Objects.isNull(properties.get(property))) {
      return null;
    } else return properties.get(property).getValue().toString();
  }

  private static CloseableHttpResponse send(
      String url, String path, String httpMethod, Map<String, String> headers, String requestBody)
      throws Exception {

    BasicHttpClientConnectionManager cm = createBasicHttpClientConnectionManager();
    headers.put("Content-Type", MediaType.APPLICATION_JSON);
    ClassicHttpRequest httpRequest =
        createHttpRequest(url + path, httpMethod, requestBody, headers);

    try (CloseableHttpClient httpClient =
        HttpClientBuilder.create().setConnectionManager(cm).build()) {
      return httpClient.execute(httpRequest, new CustomHttpClientResponseHandler());
    }
  }

  private static BasicHttpClientConnectionManager createBasicHttpClientConnectionManager() {
    ConnectionConfig connConfig =
        ConnectionConfig.custom()
            .setConnectTimeout(1500000, TimeUnit.MILLISECONDS)
            .setSocketTimeout(1500000, TimeUnit.MILLISECONDS)
            .build();
    BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
    cm.setConnectionConfig(connConfig);
    return cm;
  }

  private static ClassicHttpRequest createHttpRequest(
      String path, String httpMethod, String requestBody, Map<String, String> headers) {
    ClassicRequestBuilder classicRequestBuilder =
        ClassicRequestBuilder.create(httpMethod)
            .setUri(path)
            .setEntity(requestBody, ContentType.APPLICATION_JSON);
    headers.forEach(classicRequestBuilder::addHeader);
    return classicRequestBuilder.build();
  }
}
