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

package org.opengroup.osdu.legal.aws.tags.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.legal.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.sql.Date;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PropertiesTypeConverterTest {

    private PropertiesTypeConverter converter;
    private Properties testProperties;

    @BeforeEach
    void setUp() {
        converter = new PropertiesTypeConverter();
        testProperties = createValidProperties();
    }

    private Properties createValidProperties() {
        Properties properties = new Properties();
        properties.setCountryOfOrigin(Arrays.asList("US", "UK"));
        properties.setOriginator("TestCompany");
        properties.setContractId("Contract-123");
        properties.setDataType("Transferred Data");
        properties.setPersonalData("Personally Identifiable");
        properties.setSecurityClassification("Confidential");
        properties.setExportClassification("EAR99");
        properties.setExpirationDate(new Date(System.currentTimeMillis() + 86400000)); // tomorrow

        Map<String, Object> extensionProperties = new LinkedHashMap<>();
        extensionProperties.put("EffectiveDate", "2024-01-01T00:00:00");
        extensionProperties.put("AffiliateEnablementIndicator", true);
        properties.setExtensionProperties(extensionProperties);

        return properties;
    }

    @Test
    void transformFrom_ValidProperties_ReturnsAttributeValue() {
        // Act
        AttributeValue result = converter.transformFrom(testProperties);

        // Assert
        assertNotNull(result);
        String jsonString = result.s();
        assertTrue(jsonString.contains("\"countryOfOrigin\":[\"US\",\"UK\"]"));
        assertTrue(jsonString.contains("\"originator\":\"TestCompany\""));
        assertTrue(jsonString.contains("\"contractId\":\"Contract-123\""));
    }

    @Test
    void transformTo_ValidJson_ReturnsProperties() {
        // Arrange
        AttributeValue attributeValue = converter.transformFrom(testProperties);

        // Act
        Properties result = converter.transformTo(attributeValue);

        // Assert
        assertNotNull(result);
        assertEquals(Arrays.asList("US", "UK"), result.getCountryOfOrigin());
        assertEquals("TestCompany", result.getOriginator());
        assertEquals("Contract-123", result.getContractId());
        assertEquals("Transferred Data", result.getDataType());
        assertEquals("Personally Identifiable", result.getPersonalData());
        assertEquals("Confidential", result.getSecurityClassification());
        assertEquals("EAR99", result.getExportClassification());
        assertNotNull(result.getExtensionProperties());
        assertEquals("2024-01-01T00:00:00", result.getExtensionProperties().get("EffectiveDate"));
    }

    @Test
    void transformFrom_NullProperties_ReturnsNullAttributeValue() {
        // Act
        AttributeValue result = converter.transformFrom(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.nul());
    }

    @Test
    void transformTo_NullAttributeValue_ReturnsNull() {
        // Act
        Properties result = converter.transformTo(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testCountryOfOriginUpperCase() {
        // Arrange
        Properties properties = new Properties();
        properties.setCountryOfOrigin(Arrays.asList("us", "uk"));

        // Act
        AttributeValue attributeValue = converter.transformFrom(properties);
        Properties result = converter.transformTo(attributeValue);

        // Assert
        assertEquals(Arrays.asList("US", "UK"), result.getCountryOfOrigin());
    }

    @Test
    void testDefaultValues() {
        // Arrange
        Properties properties = new Properties();

        // Assert
        assertTrue(properties.getCountryOfOrigin().isEmpty());
        assertEquals("", properties.getDataType());
        assertEquals("", properties.getSecurityClassification());
        assertEquals("", properties.getPersonalData());
        assertEquals("", properties.getExportClassification());
        assertEquals("", properties.getOriginator());
        assertEquals("", properties.getContractId());
        assertEquals(Properties.DEFAULT_EXPIRATIONDATE, properties.getExpirationDate());
    }

    @Test
    void testContractIdValidation() {
        // Test valid contract ID
        testProperties.setContractId("Contract-123");
        assertTrue(testProperties.hasContractId());

        // Test unknown contract ID
        testProperties.setContractId(Properties.UNKNOWN_CONTRACT_ID);
        assertFalse(testProperties.hasContractId());

        // Test invalid contract ID
        testProperties.setContractId("C@ntract");
        assertFalse(testProperties.hasContractId());
    }

    @Test
    void testExpirationDateLogic() {
        // Test future date
        Date futureDate = new Date(System.currentTimeMillis() + 86400000); // tomorrow
        testProperties.setExpirationDate(futureDate);
        assertFalse(testProperties.hasExpired());

        // Test past date
        Date pastDate = new Date(System.currentTimeMillis() - 86400000); // yesterday
        testProperties.setExpirationDate(pastDate);
        assertTrue(testProperties.hasExpired());

        // Test default date
        testProperties.setExpirationDate(Properties.DEFAULT_EXPIRATIONDATE);
        assertTrue(testProperties.isDefaultExpirationDate());
    }

    @Test
    void testDataTypeChecks() {
        // Test third party data
        testProperties.setDataType(DataTypeValues.THIRD_PARTY_DATA);
        assertTrue(testProperties.hasThirdPartyDataType());
        assertFalse(testProperties.hasSecondPartyDataType());

        // Test second party data
        testProperties.setDataType(DataTypeValues.SECOND_PARTY_DATA);
        assertTrue(testProperties.hasSecondPartyDataType());
        assertFalse(testProperties.hasThirdPartyDataType());
    }
}
