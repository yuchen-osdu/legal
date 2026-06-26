package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class LegalTagCountriesServiceTests {

    private List<Country> listOfCountries;

    @Mock
    private LegalTagCountriesTenantRepositories repositories;

    @Mock
    private RequestInfo requestInfo;

    @Mock
    private ServiceConfig serviceConfig;

    @Mock
    private TenantInfo tenantInfo;

    @InjectMocks
    private LegalTagCountriesService sut;

    @Mock
    private LegalTagCountriesRepository legalTagCountriesRepository;

    @Mock
    private DefaultCountriesRepository defaultCountriesRepository;

    @Before
    public void setup() throws Exception {
        DefaultCountriesRepository defaultCountriesRepository = new DefaultCountriesRepository();
        this.listOfCountries = defaultCountriesRepository.read();
        when(this.repositories.get(any(), any())).thenReturn(this.legalTagCountriesRepository);
        when(this.legalTagCountriesRepository.read()).thenReturn(this.listOfCountries);
        when(this.requestInfo.getTenantInfo()).thenReturn(this.tenantInfo);
        when(this.defaultCountriesRepository.read()).thenReturn(this.listOfCountries);
        Whitebox.invokeMethod(sut, "setup");
    }

    @Test
    public void should_returnTransferredDataAllowedCountriesToValidCoo() {
        Map<String, String> validCoos = this.sut.getValidCOOs("Transferred Data");
        Assert.assertEquals(224, validCoos.size());
    }

    @Test
    public void should_returnNormalValidCoo_withoutSpecialDataType() {
        Map<String, String> validCoos = this.sut.getValidCOOs("Public Domain Data");
        Assert.assertEquals(59, validCoos.size());
    }

    @Test
    public void should_returnSameNumberOfCoo_withOrWithoutProvidingNoneSpecialDataType() {
        Map<String, String> validCoos = this.sut.getValidCOOs("Public Domain Data");
        Map<String, String> validCoosWithOutDataType = this.sut.getValidCOOs();
        Assert.assertEquals(true, validCoos.size() == validCoosWithOutDataType.size());
    }

    @Test
    public void should_returnEmbargoedCountriesAsORDCCountries() {
        Map<String, String> validCoos = this.sut.getValidORDCs();
        Assert.assertEquals(238, validCoos.size());
    }

    @Test
    public void should_overridePermission_whenConfigurationDiffersInCloud() {
        List<String> dataTypeAllowed = new ArrayList<>();
        dataTypeAllowed.add("Transferred Data");
        Country overwrite = new Country("Mexico", "MX", 484, "No restriction", dataTypeAllowed);
        for (int i = 0; i < this.listOfCountries.size(); i++) {
            if (this.listOfCountries.get(i).isMatchByAlpha2(overwrite)) {
                this.listOfCountries.set(i, overwrite);
            }
        }
        Map<String, String> validCoos = this.sut.getValidCOOs("Public Domain Data");
        Assert.assertEquals(60, validCoos.size());
    }

    @Test
    public void should_overridePermissionAndSetDataTypeAsEmptyArray_whenConfigurationDiffersInCloud() {
        Country overwrite = new Country("Malaysia", "MY", 458, "Client consent required", null);
        for (int i = 0; i < this.listOfCountries.size(); i++) {
            if (this.listOfCountries.get(i).isMatchByAlpha2(overwrite)) {
                this.listOfCountries.set(i, overwrite);
            }
        }
        //the empty array should already be there, or null pointer exception would be thrown
        Map<String, String> validCoos = this.sut.getValidCOOs("Public Domain Data");
        Assert.assertEquals(60, validCoos.size());
    }
}
