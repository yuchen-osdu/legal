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

package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.legal.CountryCodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertiesMock {
        public static Map<String,String> validOrdcs = Arrays.stream(CountryCodes.values()).filter(
            c -> c.getResidencyRisk() != CountryCodes.RESIDENCY_RISK.EMBARGOED)
            .collect(Collectors.toMap(CountryCodes::getAlpha2, CountryCodes::getName));

        public static Map<String,String> coos = Arrays.stream(CountryCodes.values()).filter(
            c -> c.getResidencyRisk() == CountryCodes.RESIDENCY_RISK.NO_RESTRICTION ||
                    c.getResidencyRisk() == CountryCodes.RESIDENCY_RISK.NOT_ASSIGNED ||
                    c.getResidencyRisk() == CountryCodes.RESIDENCY_RISK.CLIENT_CONSENT_REQUIRED)
            .collect(Collectors.toMap(CountryCodes::getAlpha2, CountryCodes::getName));
}
