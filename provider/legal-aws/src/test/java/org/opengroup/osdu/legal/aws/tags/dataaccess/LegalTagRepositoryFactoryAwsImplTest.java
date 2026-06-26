/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.legal.aws.tags.dataaccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class LegalTagRepositoryFactoryAwsImplTest {

    @Mock
    TenantInfo tenantInfo;

    @Mock
    ITenantFactory tenantFactory;

    @Mock
    LegalTagRepositoryImpl repoImpl;

    @InjectMocks
    LegalTagRepositoryFactoryAwsImpl legalTagRepositoryFactoryAwsImpl;

    LegalTagRepositoryFactoryAwsImpl factoryImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factoryImpl = new LegalTagRepositoryFactoryAwsImpl(tenantInfo, tenantFactory);
    }

    @Test
    void testConstructor() {
        TenantInfo reflectedTenantInfo = (TenantInfo) ReflectionTestUtils.getField(factoryImpl, "tenantInfo");
        ITenantFactory reflectedTenantFactory = (ITenantFactory) ReflectionTestUtils.getField(factoryImpl, "tenantFactory");

        assertEquals(tenantInfo, reflectedTenantInfo);
        assertEquals(tenantFactory, reflectedTenantFactory);
    }

    @Test
    void testGet_withValidTenantName() {
        String validTenantName = "validTenantName";
        TenantInfo returnedTenantInfo = new TenantInfo(); 
        ReflectionTestUtils.setField(legalTagRepositoryFactoryAwsImpl, "repoImpl", repoImpl);
        when(tenantFactory.getTenantInfo(validTenantName)).thenReturn(returnedTenantInfo);
        doNothing().when(repoImpl).setTenantInfo(returnedTenantInfo);
        ILegalTagRepository result = legalTagRepositoryFactoryAwsImpl.get(validTenantName);

        verify(repoImpl, times(1)).setTenantInfo(returnedTenantInfo);
        assertEquals(repoImpl, result);
    }

    @Test
    void testGet_withBlankTenantName() {
        assertThrows(AppException.class, () -> legalTagRepositoryFactoryAwsImpl.get("  "));
    }

    @Test
    void testGet_withNullTenantName() {
        assertThrows(AppException.class, () -> legalTagRepositoryFactoryAwsImpl.get(null));
    }
}

