/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.legal.tags;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.tags.dto.LegalTagDto;
import org.opengroup.osdu.legal.tags.dto.UpdateLegalTag;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import static org.opengroup.osdu.core.common.model.legal.Properties.DEFAULT_EXPIRATIONDATE;

import java.util.ArrayList;
import java.sql.Date;

public class LegalTestUtils {
    public static LegalTag createValidLegalTag(String name){
        LegalTag legalTag = new LegalTag();
        legalTag.setProperties(createValidProperties());
        legalTag.setName(name);
        legalTag.setIsValid(false);
        legalTag.setDefaultId();
        return legalTag;
    }
    public static Properties createValidProperties(){
        Properties properties = new Properties();
        properties.setCountryOfOrigin(new ArrayList<String>(){{add("USA");}});
        properties.setExpirationDate(new Date(System.currentTimeMillis()));
        properties.setOriginator("MyCompany");
        properties.setContractId("Unknown");
        properties.setDataType("Tranferred Data");
        properties.setPersonalData("Sensitive Personal Information");
        properties.setSecurityClassification("Confidential");
        properties.setExportClassification("ECCN");
        return properties;
    }
    public static UpdateLegalTag createUpdateLegalTag(String name){
        UpdateLegalTag legalTag = new UpdateLegalTag();
        legalTag.setExpirationDate(DEFAULT_EXPIRATIONDATE);
        legalTag.setContractId("abc123");
        legalTag.setName(name);
        legalTag.setDescription("myDescription");
        return legalTag;
    }
    public static LegalTagDto createValidLegalTagDto(String name){
        return LegalTagDto.convertTo(createValidLegalTag(name));
    }
}
