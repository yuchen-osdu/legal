#!/usr/bin/env bash
#  Copyright 2023 Google LLC
#  Copyright 2023 EPAM
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License. 

{ set +x ;} 2> /dev/null # disable output to prevent secret logging
set -e

ENV_VAR_NAME=$1

if [ "${!ENV_VAR_NAME}" = "" ]
then
    echo "Missing environment variable '$ENV_VAR_NAME'. Please provide all variables and try again"
    { set -x ;} 2> /dev/null # enable output back
    exit 1
fi

{ set -x ;} 2> /dev/null # enable output back
