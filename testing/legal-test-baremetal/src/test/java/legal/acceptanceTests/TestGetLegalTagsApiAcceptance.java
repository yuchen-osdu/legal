package legal.acceptanceTests;

import legal.util.AnthosLegalTagUtils;
import org.junit.After;
import org.junit.Before;
import org.opengroup.osdu.legal.acceptanceTests.GetLegalTagsApiAcceptanceTests;

public class TestGetLegalTagsApiAcceptance extends GetLegalTagsApiAcceptanceTests {

    @Before
    @Override
    public void setup() throws Exception {
        this.legalTagUtils = new AnthosLegalTagUtils();
        super.setup();
    }

    @After
    @Override
    public void teardown() throws Exception {
        super.teardown();
        this.legalTagUtils = null;
    }

}
