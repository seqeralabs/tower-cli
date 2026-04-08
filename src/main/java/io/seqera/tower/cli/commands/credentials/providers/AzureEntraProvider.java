/*
 * Copyright 2021-2026, Seqera.
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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AzureEntraKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzureEntraProvider extends AbstractProvider<AzureEntraKeys> {

    @Option(names = {"--batch-name"}, description = "Azure Batch account name. The name of the Azure Batch account used for workflow execution.", required = true)
    public String batchName;

    @Option(names = {"--storage-name"}, description = "Azure Storage account name. The name of the Azure Storage account used for workflow data and logs.", required = true)
    public String storageName;

    @Option(names = {"--tenant-id"}, description = "Azure Entra tenant ID. The directory (tenant) ID of the Entra application.", required = true)
    public String tenantId;

    @Option(names = {"--client-id"}, description = "Azure Entra client ID. The application (client) ID of the Entra service principal.", required = true)
    public String clientId;

    @Option(names = {"--client-secret"}, description = "Azure Entra client secret. The secret value of the Entra service principal.", required = true)
    public String clientSecret;

    public AzureEntraProvider() {
        super(ProviderEnum.AZURE_ENTRA);
    }

    @Override
    public AzureEntraKeys securityKeys() throws IOException {
        AzureEntraKeys keys = new AzureEntraKeys();
        keys.setBatchName(batchName);
        keys.setStorageName(storageName);
        keys.setTenantId(tenantId);
        keys.setClientId(clientId);
        keys.setClientSecret(clientSecret);
        return keys;
    }
}
