/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.legal;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;

public class InvalidCooRule extends Rule {
    @Override
    public boolean shouldCheck(Properties properties) {
        return true;
    }

    @Override
    protected String hasError(Properties properties) {return String.format("Invalid country of origin set. It should match one of the ISO alpha 2 codes and be a country with no restriction on data residency. Found: %s.", properties.getCountryOfOrigin());}
}
