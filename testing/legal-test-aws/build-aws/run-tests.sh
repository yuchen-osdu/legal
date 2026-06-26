# Copyright © Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script executes the test and copies reports to the provided output directory
# To call this script from the service working directory
# ./dist/testing/integration/build-aws/run-tests.sh "./reports/"


SCRIPT_SOURCE_DIR=$(dirname "$0")
echo "Script source location"
echo "$SCRIPT_SOURCE_DIR"
(cd "$SCRIPT_SOURCE_DIR"/../bin && ./install-deps.sh)

#### ADD REQUIRED ENVIRONMENT VARIABLES HERE ###############################################
# The following variables are automatically populated from the environment during integration testing
# see os-deploy-aws/build-aws/integration-test-env-variables.py for an updated list

### DYNAMIC PARMETERS ###
# AWS_COGNITO_CLIENT_ID
# ELASTIC_HOST
# ELASTIC_PORT
# FILE_URL
# LEGAL_URL
# TENANT_GROUP_NAME
# SEARCH_URL
# LEGAL_QUEUE
# LEGAL_S3_BUCKET

### STATIC PARAMETERS ###: KEEP IN ALPHABETICAL ORDER 
# ** DO NOT ADD VARIABLES NOT USED BY THIS SERVICE!!!! **
export AWS_COGNITO_AUTH_FLOW=USER_PASSWORD_AUTH
export AWS_COGNITO_AUTH_PARAMS_PASSWORD=$ADMIN_PASSWORD
export AWS_COGNITO_AUTH_PARAMS_USER=$ADMIN_USER
export AWS_COGNITO_CLIENT_ID=$AWS_COGNITO_CLIENT_ID
if [ -z "$LEGAL_S3_ENDPOINT" ]
then
    export AWS_S3_ENDPOINT=s3.$AWS_REGION.amazonaws.com
else
    export AWS_S3_ENDPOINT=$LEGAL_S3_ENDPOINT
fi

export AWS_S3_REGION=$AWS_REGION
if [ -z "$LEGAL_DYNAMODB_ENDPOINT" ]
then
    export DYNAMO_DB_ENDPOINT=dynamodb.$AWS_REGION.amazonaws.com
else
    export DYNAMO_DB_ENDPOINT=$LEGAL_DYNAMODB_ENDPOINT

fi
export DYNAMO_DB_REGION=$AWS_REGION
export HOST_URL=$LEGAL_URL
export MY_TENANT=int-test-legal
export MY_SECOND_TENANT=opendes
export S3_LEGAL_CONFIG_BUCKET=$LEGAL_S3_BUCKET
export SKIP_HTTP_TESTS=true
export TABLE_PREFIX=$TENANT_GROUP_NAME


#### RUN INTEGRATION TEST #########################################################################
JAVA_HOME=$JAVA17_HOME

mvn  -ntp test -f "$SCRIPT_SOURCE_DIR"/../pom.xml
TEST_EXIT_CODE=$?

#### COPY TEST REPORTS #########################################################################

if [ -n "$1" ]
  then
    mkdir -p "$1"
    cp -R "$SCRIPT_SOURCE_DIR"/../target/surefire-reports "$1"
fi

exit $TEST_EXIT_CODE
