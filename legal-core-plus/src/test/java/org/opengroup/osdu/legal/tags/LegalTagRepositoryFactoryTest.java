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
package org.opengroup.osdu.legal.tags;

import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dataaccess.OsmLegalTagRepository;

public class LegalTagRepositoryFactoryTest {

  private static final String TENANT_1 = "tenant1";
  private OsmLegalTagRepository osmLegalTagRepository;

  @Before
  public void init() {
  }

  @Test(expected = AppException.class)
  public void should_throwAppException_when_givenBlankName() {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setName(TENANT_1);
    ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(tenantInfo,
        osmLegalTagRepository);
    sut.get("");
  }

  @Test(expected = AppException.class)
  public void should_throwAppException_when_tenantIsNull() {
    TenantInfo tenantInfo = new TenantInfo();
    ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(tenantInfo,
        osmLegalTagRepository);
    sut.get(null);
  }

}
