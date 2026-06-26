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

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dataaccess.OsmLegalTagRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Log
@RequiredArgsConstructor
public class LegalTagRepositoryFactoryGcpImpl implements ILegalTagRepositoryFactory {

  private final Map<String, ILegalTagRepository> tenantRepositories = new HashMap<>();
  private final TenantInfo tenantInfo;
  private final OsmLegalTagRepository osmLegalTagRepository;


  @Override
  public ILegalTagRepository get(String tenantName) {
    if (StringUtils.isBlank(tenantName)) {
      throw invalidTenantGivenException(tenantName);
    }
    return osmLegalTagRepository;
  }

  AppException invalidTenantGivenException(String tenantName) {
    return new AppException(403, "Forbidden",
        String.format("You do not have access to the %s, value given %s",
            DpsHeaders.DATA_PARTITION_ID, tenantName));
  }
}
