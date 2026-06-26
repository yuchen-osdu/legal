# IBM's backend for legal

## A note about authentication and entitlements

OAuth2 JWT token authentication is now enforced

## A note about publishing legal tag changes

This is not implemented yet, the Bean that takes the pubsub requests is a Mock.
However, since we are using cloudant, we could keep it that way and leverage
cloudant's change stream

## Testing

For testing a Cloudant instance is needed. Once you have a cloudant instance
in IBMCloud, download the credentials JSON somewhere, and point the IBM_CREDENTIALS_FILE
environment variable to it:

    $> export IBM_CREDENTIALS_FILE=/path/to/credentials.json


### Running the unit tests

To run the unit tests, go to `provider/legal-ibm` and run:

    $> mvn test

### Running the acceptance tests

For this it ideal to open another terminal with the IBM_CREDENTIALS_FILE variable set also.

In one terminal go to `testing/legal-test-ibm` and run:

    $> setup_acceptance.sh
    $> run_service.sh

In the other, also go to `testing/legal-test-ibm` and run: 

    $> run_tests.sh

To delete the test databases, just run:

    $> teardown_acceptance.sh
