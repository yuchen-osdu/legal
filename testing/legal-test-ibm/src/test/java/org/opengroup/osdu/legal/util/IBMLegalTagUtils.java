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

package org.opengroup.osdu.legal.util;

import org.opengroup.osdu.core.ibm.util.Config;
import org.opengroup.osdu.core.ibm.util.IdentityClient;

import com.google.common.base.Strings;

import java.io.IOException;
import java.util.List;

public class IBMLegalTagUtils extends LegalTagUtils {

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
    	// not needed for IBM
    }
    
    @Override
    public synchronized String accessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            token = IdentityClient.getTokenForUserWithAccess();
        }
        return "Bearer " + token;
    }

    @Override
    public List<String> readCOOCountries(String storageAccount, String defaultCOOFileName) throws IOException {
        return List.of();
    }

    private static final String mqConnection = "IBM_LEGAL_MQ_CONNECTION";
    private static final String envPrefix = "IBM_ENV_PREFIX";
    
    public static String getMessageQueueConnectionString() {
		return Config.getEnvironmentVariable(mqConnection);
    }
    
	public static String getQueueName() {
	    return Config.getEnvironmentVariable(envPrefix);
    }

}
