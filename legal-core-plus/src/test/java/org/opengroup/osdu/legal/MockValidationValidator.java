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

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;

import jakarta.validation.*;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockValidationValidator extends LegalTagConstraintValidator {

    private final String rulesetName;

    public MockValidationValidator(String ruleSet, RulesetProvider rsp){
        this.rulesetName = ruleSet;
        this.ruleSetProvider = rsp;
    }
    public MockValidationValidator(){
        this.rulesetName = TenantInfo.ComplianceRuleSets.SHARED;
        this.ruleSetProvider = CreateRsp(new MockRule());
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
        this.requestInfo = mock(RequestInfo.class);
        when(requestInfo.getComplianceRuleSet()).thenReturn(rulesetName);
        return super.getInstance(key);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }

    public static Validator GetValidator(String rulesetName, RulesetProvider rsp){
        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(new MockValidationValidator(rulesetName, rsp));
        ValidatorFactory factory = config.buildValidatorFactory();
        return factory.getValidator();
    }

    public static Validator GetValidator(String rulesetName){
        Configuration<?> config = Validation.byDefaultProvider().configure();
        config.constraintValidatorFactory(new MockValidationValidator(rulesetName, CreateRsp(new MockRule())));
        ValidatorFactory factory = config.buildValidatorFactory();
        return factory.getValidator();
    }
    public static Validator GetValidator(){
        Configuration<?> config = Validation.byDefaultProvider().configure();
        return GetValidator(TenantInfo.ComplianceRuleSets.SHARED);
    }

    public static RulesetProvider CreateRsp(Rule... rules){
        RulesetProvider rsp = mock(RulesetProvider.class);
        when(rsp.get()).thenReturn(Arrays.asList(rules));
        return rsp;
    }
}
