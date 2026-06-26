package org.opengroup.osdu.legal.countries;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class LegalTagCountriesRepositoryImplTests {
    @Mock
    private IStorageReader storageReader;

    @InjectMocks
    private LegalTagCountriesRepositoryImpl sut;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_returnEmptyCountries_given_empty_blob() {
        byte[] bytes = "".getBytes();
        when(this.storageReader.readAllBytes()).thenReturn(bytes);

        List<Country> countries = sut.read();
        assertTrue(countries.isEmpty());
    }

    @Test
    public void should_returnExpectingCountries_given_countriesJsonString() {

        String jsonString = generateJsonString();
        byte[] bytes = jsonString.getBytes();
        when(this.storageReader.readAllBytes()).thenReturn(bytes);
        List<Country> countries = sut.read();
        assertEquals(2, countries.size());
        assertEquals("AD", countries.get(0).getAlpha2());
        assertEquals("AE", countries.get(1).getAlpha2());

    }

    private String generateJsonString() {
        return "[{\n" +
                "    \"name\":\"Andorra\",\n" +
                "    \"alpha2\":\"AD\",\n" +
                "    \"numeric\":\"16\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\":\"United Arab Emirates\",\n" +
                "    \"alpha2\":\"AE\",\n" +
                "    \"numeric\":\"784\"\n" +
                "  }]";
    }

}
