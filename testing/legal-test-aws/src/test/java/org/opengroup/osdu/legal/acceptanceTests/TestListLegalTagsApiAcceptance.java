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

package org.opengroup.osdu.legal.acceptanceTests;

import static org.junit.Assert.*;
import static org.opengroup.osdu.legal.util.Constants.DATA_PARTITION_ID;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.legal.util.AwsLegalTagUtils;
import org.opengroup.osdu.legal.util.LegalTagUtils;
import org.opengroup.osdu.legal.util.TestUtils;

import com.sun.jersey.api.client.ClientResponse;

public class TestListLegalTagsApiAcceptance extends ListLegalTagsApiAcceptanceTests {

    private String primaryTagName = String.format("%s%s", "int-test-legal-primary-", String.valueOf(System.currentTimeMillis()));
    private String secondaryTagName = String.format("%s%s", "int-test-legal-secondary-", String.valueOf(System.currentTimeMillis()));
    
    // Use CUSTOM_TENANT for different partition testing
    private String primaryPartition = TestUtils.getMyDataPartition();
    private String secondaryPartition = TestUtils.getMySecondDataPartition();
    
    @Before
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new AwsLegalTagUtils();

        // Create valid tag in the primary partition
        ClientResponse primaryResponse = this.legalTagUtils.create("US", primaryTagName, "2099-12-25", "Transferred Data", primaryPartition, "<my description>");
        assertEquals(201, primaryResponse.getStatus());

        // Create a valid tag with the same name in the secondary partition
        ClientResponse secondaryResponse = this.legalTagUtils.create("US", secondaryTagName, "2099-12-25", "Transferred Data", secondaryPartition, "<my description>");
        assertEquals(201, secondaryResponse.getStatus());

        super.setup();
    }


    @After
    @Override
    public void teardown() throws Exception {
        super.teardown();

        // Clean up tags in both partitions
        this.legalTagUtils.delete(primaryTagName, primaryPartition);
        this.legalTagUtils.delete(secondaryTagName, secondaryPartition);

        this.legalTagUtils = null;
    }
    
    @Test
    public void should_onlyReturnTagsFromSamePartition_when_listingLegalTags() throws Exception {
        // Get tags from primary partition using valid=true query parameter
        ClientResponse response = send("", 200, "?valid=true", primaryPartition);

        // Parse the response
        LegalTagUtils.ReadableLegalTags result = legalTagUtils.getResult(response, 200, LegalTagUtils.ReadableLegalTags.class);

        // Check if secondary tag is found in the primary partition results
        boolean foundSecondaryTag = false;
        for (LegalTagUtils.ReadableLegalTag tag : result.legalTags) {
            if (tag.name.equals(secondaryTagName)) {
                foundSecondaryTag = true;
                break;
            }
        }

        // Secondary tag should not be found in primary partition results
        assertFalse("Primary partition should NOT contain the secondary tag from different partition", foundSecondaryTag);
    }
}
