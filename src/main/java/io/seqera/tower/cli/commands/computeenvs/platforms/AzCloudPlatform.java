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

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an Azure Blob Storage path (e.g., az://your-container/work). Credentials must have read-write access.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "Azure region where virtual machines will be deployed (e.g., eastus, westeurope).", required = true)
    public String region;

    @Option(names = {"--resource-group"}, description = "Azure resource group for organizing and managing virtual machines. The resource group must already exist in the subscription.", required = true)
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
        @Option(names = {"--data-collection-endpoint"}, description = "Azure Monitor data collection endpoint URL for log ingestion. Used to route logs to Log Analytics workspace.")
        public String dataCollectionEndpoint;

        @Option(names = {"--data-collection-rule-id"}, description = "Azure Monitor data collection rule ID. Defines how logs are processed and routed to destination workspaces.")
        public String dataCollectionRuleId;

        @Option(names = {"--instance-type"}, description = "Azure virtual machine size (e.g., Standard_D2s_v3, Standard_E4s_v3). If omitted, a default VM size is used.")
        public String instanceType;

        @Option(names = {"--log-table-name"}, description = "Custom table name in Log Analytics workspace for storing compute environment logs. Enables organized log management.")
        public String logTableName;

        @Option(names = {"--log-workspace-id"}, description = "Azure Log Analytics workspace ID for monitoring compute environment activity and logs.")
        public String logWorkspaceId;

        @Option(names = {"--managed-identity-client-id"}, description = "User-assigned managed identity client ID for authentication. Used with managed identity resource ID for VM access control.")
        public String managedIdentityClientId;

        @Option(names = {"--managed-identity-id"}, description = "User-assigned managed identity resource ID. Provides VMs with Azure resource access without storing credentials.")
        public String managedIdentityId;

        @Option(names = {"--network-id"}, description = "Azure virtual network resource ID. Defines the network where VMs will be deployed for network isolation and connectivity.")
        public String networkId;

        @Option(names = {"--subscription-id"}, description = "Azure subscription ID where resources will be created. Used to specify the billing and access control boundary.")
        public String subscriptionId;
    }
}
