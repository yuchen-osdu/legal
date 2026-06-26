package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import junit.framework.TestCase;
import org.junit.Test;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LegalTagCountriesTenantRepositoriesTests {

    @Mock
    private IStorageReader storageReader;

    @Mock
    private IStorageReaderFactory storageReaderFactory;

    @InjectMocks
    private LegalTagCountriesTenantRepositories sut;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = AppException.class)
    public void should_throwAppException_when_givenBlankName(){
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("");
        sut.get(tenantInfo, "us");
    }

    @Test
    public void should_returnRepo_when_requestingTenantThatHasNotPreviouslyBeenRequested(){
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("tenant1");
        when(storageReaderFactory.getReader(tenantInfo, "us")).thenReturn(storageReader);
        LegalTagCountriesRepository result = sut.get(tenantInfo, "us");
        TestCase.assertNotNull(result);
    }
}
