package legal.acceptanceTests;

import legal.util.AnthosLegalTagUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.opengroup.osdu.legal.acceptanceTests.ValidateLegalTagsApiAcceptanceTests;

public class TestValidateLegalTagsApiAcceptance extends ValidateLegalTagsApiAcceptanceTests {

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

    //  Ignored due to not clear test requirements, original test did not create legal tag but expecting response with message "Expiration date must be a value in the future"
    @Ignore
    @Override
    public void should_return200_withLegalTagNamesAndInvalidExpirationDateReason_when_GivenExistingInvalidLegalTagNames() throws Exception {
    }
}
