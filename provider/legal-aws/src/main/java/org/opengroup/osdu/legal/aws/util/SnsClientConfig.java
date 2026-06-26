/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.legal.aws.util;

import org.opengroup.osdu.core.aws.v2.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.v2.ssm.K8sParameterNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SnsClientConfig {

    @Value("${aws.sns.region}")
    private String amazonSNSRegion;

    @Bean
    public SnsClient amazonSNS() {
        AmazonSNSConfig config = new AmazonSNSConfig(amazonSNSRegion);
        return config.AmazonSNS();
    }

    @Bean
    public String amazonSNSTopic() throws K8sParameterNotFoundException {
        K8sLocalParameterProvider provider = new K8sLocalParameterProvider();
        return provider.getParameterAsString("legal-sns-topic-arn");
    }

}
