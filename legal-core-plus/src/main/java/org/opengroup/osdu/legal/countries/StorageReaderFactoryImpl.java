/*
 *  Copyright 2020-2024 Google LLC
 *  Copyright 2020-2024 EPAM Systems, Inc
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

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.legal.config.PartitionPropertyNames;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageReaderFactoryImpl implements IStorageReaderFactory {

  private final Driver storage;
  private final PartitionPropertyResolver partitionPropertyResolver;
  private final PartitionPropertyNames partitionPropertyNames;
  private final ICache<String, byte[]> legalCOOCache;

  @Override
  public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
    return new StorageReaderImpl(
        tenant, storage, partitionPropertyResolver, partitionPropertyNames, legalCOOCache);
  }
}
