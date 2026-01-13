/*
 * Copyright 2021-2023, Seqera.
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

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.AzCloudConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzCloudPlatform extends AbstractPlatform<AzCloudConfig> {

    @Option(names = {"--work-dir"}, description = "Work directory.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "Azure region.", required = true)
    public String region;

    @Option(names = {"--resource-group"}, description = "Azure resource group name.", required = true)
    public String resourceGroup;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AzCloudPlatform() {
        super(PlatformEnum.AZURE_CLOUD);
    }

    @Override
    public AzCloudConfig computeConfig() throws ApiException, IOException {
        AzCloudConfig config = new AzCloudConfig();

        config
                .waveEnabled(true)
                .fusion2Enabled(true)

                // Main
                .region(region)
                .resourceGroup(resourceGroup);

        // Advanced
        if (adv != null) {
            config
                    .instanceType(adv.instanceType)
                    .subscriptionId(adv.subscriptionId)
                    .networkId(adv.networkId)
                    .managedIdentityId(adv.managedIdentityId)
                    .managedIdentityClientId(adv.managedIdentityClientId)
                    .logWorkspaceId(adv.logWorkspaceId)
                    .logTableName(adv.logTableName)
                    .dataCollectionEndpoint(adv.dataCollectionEndpoint)
                    .dataCollectionRuleId(adv.dataCollectionRuleId);
        }

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    public static class AdvancedOptions {
        @Option(names = {"--data-collection-endpoint"}, description = "Data collection endpoint URL.")
        public String dataCollectionEndpoint;

        @Option(names = {"--data-collection-rule-id"}, description = "Data collection rule ID.")
        public String dataCollectionRuleId;

        @Option(names = {"--instance-type"}, description = "Azure virtual machine type.")
        public String instanceType;

        @Option(names = {"--log-table-name"}, description = "Log Analytics table name.")
        public String logTableName;

        @Option(names = {"--log-workspace-id"}, description = "Log Analytics workspace ID.")
        public String logWorkspaceId;

        @Option(names = {"--managed-identity-client-id"}, description = "Managed identity client ID.")
        public String managedIdentityClientId;

        @Option(names = {"--managed-identity-id"}, description = "Managed identity resource ID.")
        public String managedIdentityId;

        @Option(names = {"--network-id"}, description = "Azure virtual network ID.")
        public String networkId;

        @Option(names = {"--subscription-id"}, description = "Azure subscription ID.")
        public String subscriptionId;
    }
}
