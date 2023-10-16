/*
 * Copyright 2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AzureSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzureProvider extends AbstractProvider<AzureSecurityKeys> {

    @Option(names = {"--batch-key"}, description = "Azure batch account key.", required = true)
    public String batchKey;

    @Option(names = {"--batch-name"}, description = "Azure batch account name.", required = true)
    public String batchName;

    @Option(names = {"--storage-key"}, description = "Azure blob storage account key.", required = true)
    public String storageKey;

    @Option(names = {"--storage-name"}, description = "Azure blob storage account name.", required = true)
    public String storageName;

    public AzureProvider() {
        super(ProviderEnum.AZURE);
    }

    @Override
    public AzureSecurityKeys securityKeys() throws IOException {
        return new AzureSecurityKeys()
                .batchKey(batchKey)
                .batchName(batchName)
                .storageKey(storageKey)
                .storageName(storageName);
    }
}
