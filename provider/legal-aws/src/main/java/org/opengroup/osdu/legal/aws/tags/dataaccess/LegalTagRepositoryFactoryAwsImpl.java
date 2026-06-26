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


import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.inject.Inject;


@ConditionalOnProperty(prefix = "repository", name = "implementation",havingValue = "dynamodb",
		matchIfMissing = true)
@Service
@Primary
@RequestScope
public class LegalTagRepositoryFactoryAwsImpl implements ILegalTagRepositoryFactory {


	private TenantInfo tenantInfo;
	private final ITenantFactory tenantFactory;

	@Inject
	LegalTagRepositoryImpl repoImpl;

	public LegalTagRepositoryFactoryAwsImpl(TenantInfo tenantInfo, ITenantFactory tenantFactory) {
		this.tenantInfo = tenantInfo;
		this.tenantFactory = tenantFactory;
	}

	@Override
	public ILegalTagRepository get(String tenantName) {
		if (StringUtils.isBlank(tenantName)) {
			throw invalidTenantGivenException(tenantName);
		}
		tenantInfo = tenantFactory.getTenantInfo(tenantName);
		repoImpl.setTenantInfo(tenantInfo);

		return repoImpl;
	}

	AppException invalidTenantGivenException(String tenantName) {
		return new AppException(403, "Forbidden",
				String.format("You do not have access to the %s value given %s",
						DpsHeaders.DATA_PARTITION_ID, tenantName));
	}
}
