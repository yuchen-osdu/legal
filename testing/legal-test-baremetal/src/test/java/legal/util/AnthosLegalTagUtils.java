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

package legal.util;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.legal.util.LegalTagUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AnthosLegalTagUtils extends LegalTagUtils {

    private static final OpenIDTokenProvider tokenProvider = new OpenIDTokenProvider();

    public AnthosLegalTagUtils() {
    }

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
    }

    @Override
    public synchronized String accessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            token = tokenProvider.getToken();
        }
        return "Bearer " + token;
    }

    @Override
    public List<String> readCOOCountries(String storageAccount, String defaultCOOFileName) throws IOException {
        return List.of();
    }
}
