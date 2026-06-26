package org.opengroup.osdu.legal.tags.model;

import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AllowedLegaltagPropertyValuesTests {
    @InjectMocks
    AllowedLegaltagPropertyValues sut;
    @Mock
    DataTypeValues dataTypeValues;
    @Mock
    RequestInfo requestInfo;

    @Before
    public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void should_returnMyCompanyDataTypes_when_RequestInfoIsMyCompanyTenant(){
        Set<String> dts = new HashSet<String>(){{add("string");}};
        when(dataTypeValues.getDataTypeValues("MyCompany")).thenReturn(dts);

        when(requestInfo.getComplianceRuleSet()).thenReturn("MyCompany");

        assertEquals(dts, sut.getDataTypes());
    }
}
