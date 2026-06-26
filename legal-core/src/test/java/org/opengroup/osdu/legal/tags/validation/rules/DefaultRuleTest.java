package org.opengroup.osdu.legal.tags.validation.rules;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.legal.tags.LegalTestUtils;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRuleTest {

    @Mock
    private LegalTagCountriesService legalTagCountriesService;

    @InjectMocks
    DefaultRule sut ;

    @Test
    public void should_handleInvalidCountryOfOrigin(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setContractId("ABC-123");
        p.setCountryOfOrigin(Arrays.asList("TT"));
        p.setExpirationDate(new Date(System.currentTimeMillis()));
        Map<String, String> validCOOs = new HashMap<>();
        validCOOs.put("USA", "USA");
        validCOOs.put("GB", "United Kingdom");
        when(legalTagCountriesService.getValidCOOs(any())).thenReturn(validCOOs);
        assertEquals("Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: [TT].",  sut.hasError(p));
    }

    @Test
    public void should_handleAppException(){
        Properties p = LegalTestUtils.createValidProperties();
        p.setContractId("ABC-123");
        p.setExpirationDate(new Date(System.currentTimeMillis()));
        AppException appException = new AppException(500, "internal error", "internal error");
        when(legalTagCountriesService.getValidCOOs(any())).thenThrow(appException);
        assertEquals("internal error",  sut.hasError(p));
    }

}