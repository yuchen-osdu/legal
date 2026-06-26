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

package org.opengroup.osdu.legal.aws.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AwsStatusChangedTagTest {

    @Mock
    private Enum mockEnum;

    private AwsStatusChangedTag awsStatusChangedTag;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        awsStatusChangedTag = new AwsStatusChangedTag("testName", mockEnum, "partition123");
    }

    @Test
    void testConstructor() {
        assertEquals("testName", awsStatusChangedTag.getChangedTagName());
        assertEquals(mockEnum, awsStatusChangedTag.getChangedTagStatus());
        assertEquals("partition123", awsStatusChangedTag.getDataPartitionId());
    }

}
