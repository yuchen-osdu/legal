package org.opengroup.osdu.legal.tags.validation;

import org.opengroup.osdu.core.common.model.legal.validation.DataTypeValidator;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataTypeValidatorTests {

    @Mock
    RequestInfo requestInfo;

    @InjectMocks
    DataTypeValidator sut;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        when(requestInfo.getComplianceRuleSet()).thenReturn(TenantInfo.ComplianceRuleSets.SHARED);
    }

    @Test
    public void should_ReturnTrue_When_GivenValidDataType(){
        assertTrue(sut.isValid(DataTypeValues.FIRST_PARTY_DATA, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenEmptyDataType(){
        assertFalse(sut.isValid("", null));
    }
    @Test
    public void should_ReturnFalse_When_GivenNullDataType(){
        assertFalse(sut.isValid(null, null));
    }
    @Test
    public void should_ReturnFalse_When_GivenInValidDataType(){
        assertFalse(sut.isValid("gb2", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenIrregularCasedValidValue(){
        assertTrue(sut.isValid("puBLIC doMAIN DATa", null));
    }
    @Test
    public void should_ReturnTrue_When_GivenSecondaryDataAsDatatypeInSharedTenant(){
        when(requestInfo.getComplianceRuleSet()).thenReturn(TenantInfo.ComplianceRuleSets.SHARED);
        assertTrue(sut.isValid("Second Party Data", null));
    }

}
