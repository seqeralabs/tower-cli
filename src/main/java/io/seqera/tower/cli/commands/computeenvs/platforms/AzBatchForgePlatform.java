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

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.CredentialsApi;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.AzBatchForgeConfig;
import io.seqera.tower.model.AzBatchPoolConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.Credentials;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AzBatchForgePlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an Azure Blob Storage path.", required = true)
    public String workDir;

    @Option(names = {"-l", "--location"}, description = "Azure region where compute resources will be deployed (e.g., eastus, westeurope).", required = true)
    public String location;

    @Option(names = {"--dual-pool"}, description = "Enable dual pool mode with separate head and worker pools. Head pool runs the Nextflow launcher on a small VM; worker pool scales independently for pipeline tasks.")
    public boolean dualPool;

    @Option(names = {"--vm-type"}, description = "Azure VM size for compute pool (single pool mode). Must be a valid Azure Batch VM type. If absent, Platform defaults to Standard_D4_v3.")
    public String vmType;

    @Option(names = {"--vm-count"}, description = "Number of VMs in the Batch pool (single pool mode). With autoscaling enabled, this is the maximum capacity. Pool scales to zero when unused.")
    public Integer vmCount;

    @Option(names = {"--no-auto-scale"}, description = "Disable pool autoscaling (single pool mode). When disabled, pool maintains fixed VM count and does not scale based on workload.")
    public boolean noAutoScale;

    @ArgGroup(heading = "%nDual pool options (head pool):%n", validate = false)
    public HeadPoolOptions headPoolOpts;

    @ArgGroup(heading = "%nDual pool options (worker pool):%n", validate = false)
    public WorkerPoolOptions workerPoolOpts;

    @Option(names = {"--preserve-resources"}, description = "Preserve Azure Batch pool resources on deletion. Keeps the compute pool and related resources when the compute environment is deleted from Seqera Platform.")
    public boolean preserveResources;

    @Option(names = {"--registry-credentials"}, split = ",", paramLabel = "<credential_name>", description = "Container registry credentials for private registries. Comma-separated list of credential names to access private Docker registries.")
    public List<String> registryCredentials;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to Azure Blob Storage with low-latency I/O. Requires Wave containers.")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Enable Wave containers. Allows access to private container repositories and on-demand container provisioning.")
    public boolean wave;

    @Option(names = {"--subnet-id"}, description = "Azure VNet subnet resource ID for private network isolation. Requires Entra (service principal) credentials. Format: /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.Network/virtualNetworks/{vnet}/subnets/{subnet}")
    public String subnetId;

    @ArgGroup(heading = "%nManaged identity options:%n", validate = false)
    public ManagedIdentityOptions managedIdentity;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AzBatchForgePlatform() {
        super(PlatformEnum.AZURE_BATCH);
    }

    @Override
    public AzBatchConfig computeConfig(Long workspaceId, CredentialsApi credentialsApi) throws ApiException, IOException {
        AzBatchConfig config = new AzBatchConfig();

        config.fusion2Enabled(fusionV2)
                .waveEnabled(wave)
                .region(location);
        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());



        if (adv != null) {
            config
                    .deleteJobsOnCompletion(adv.deleteJobsOnCompletion)
                    .deleteTasksOnCompletion(adv.deleteTasksOnCompletion)
                    .terminateJobsOnCompletion(adv.terminateJobsOnCompletion)
                    .tokenDuration(adv.tokenDuration)
                    .jobMaxWallClockTime(adv.jobMaxWallClockTime);
        }


        AzBatchForgeConfig forge = new AzBatchForgeConfig()
                .disposeOnDeletion(!preserveResources);

        if (dualPool) {
            AzBatchPoolConfig headPool = new AzBatchPoolConfig();
            if (headPoolOpts != null) {
                headPool.vmType(headPoolOpts.headVmType);
                headPool.vmCount(headPoolOpts.headVmCount);
                headPool.autoScale(headPoolOpts.headNoAutoScale != null ? !headPoolOpts.headNoAutoScale : null);
            }

            AzBatchPoolConfig workerPool = new AzBatchPoolConfig();
            if (workerPoolOpts != null) {
                workerPool.vmType(workerPoolOpts.workerVmType);
                workerPool.vmCount(workerPoolOpts.workerVmCount);
                workerPool.autoScale(workerPoolOpts.workerNoAutoScale != null ? !workerPoolOpts.workerNoAutoScale : null);
            }

            forge.headPool(headPool);
            forge.workerPool(workerPool);
        } else {
            forge.vmType(vmType)
                    .vmCount(vmCount)
                    .autoScale(!noAutoScale);
        }

        if (registryCredentials != null && registryCredentials.size() > 0) {

            Map<String, String> credentialsNameToId = new HashMap<>();
            List<Credentials> credentials = credentialsApi.listCredentials(workspaceId, null).getCredentials();
            if (credentials != null) {
                for (Credentials c : credentials) {
                    if (c.getProvider() == Credentials.ProviderEnum.CONTAINER_REG) {
                        credentialsNameToId.put(c.getName(), c.getId());
                    }
                }
            }

            for (String name : registryCredentials) {
                if (credentialsNameToId.containsKey(name)) {
                    forge.addContainerRegIdsItem(credentialsNameToId.get(name));
                } else {
                    throw new CredentialsNotFoundException(name, workspaceId);
                }
            }
        }

        config.forge(forge);
        config.subnetId(subnetId);

        if (managedIdentity != null) {
            config.managedIdentityClientId(managedIdentity.managedIdentityClientId);
            config.managedIdentityPoolClientId(managedIdentity.managedIdentityPoolClientId);
            config.managedIdentityHeadResourceId(managedIdentity.managedIdentityHeadResourceId);
            config.managedIdentityPoolResourceId(managedIdentity.managedIdentityPoolResourceId);
        }

        return config;
    }

    public static class HeadPoolOptions {

        @Option(names = {"--head-vm-type"}, description = "Azure VM size for the head pool (dual pool mode). If absent, defaults to Standard_D2s_v3.")
        public String headVmType;

        @Option(names = {"--head-vm-count"}, description = "Number of VMs in the head pool (dual pool mode). If absent, defaults to 1.")
        public Integer headVmCount;

        @Option(names = {"--head-no-auto-scale"}, description = "Disable autoscaling for the head pool (dual pool mode).")
        public Boolean headNoAutoScale;

    }

    public static class WorkerPoolOptions {

        @Option(names = {"--worker-vm-type"}, description = "Azure VM size for the worker pool (dual pool mode). If absent, defaults to Standard_D4s_v3.")
        public String workerVmType;

        @Option(names = {"--worker-vm-count"}, description = "Max number of VMs in the worker pool (dual pool mode).")
        public Integer workerVmCount;

        @Option(names = {"--worker-no-auto-scale"}, description = "Disable autoscaling for the worker pool (dual pool mode).")
        public Boolean workerNoAutoScale;

    }

    public static class ManagedIdentityOptions {

        @Option(names = {"--managed-identity-client-id"}, description = "Head job managed identity client ID (UUID). The user-assigned managed identity used by the Nextflow launcher (head job). Mirrors the API field `managedIdentityClientId`; the `managed-identity-pool-*` / `managed-identity-head-resource-id` naming asymmetry is inherited from the SDK model.")
        public String managedIdentityClientId;

        @Option(names = {"--managed-identity-pool-client-id"}, description = "Compute job managed identity client ID (UUID). The user-assigned managed identity used by compute tasks running on Batch pool nodes.")
        public String managedIdentityPoolClientId;

        @Option(names = {"--managed-identity-head-resource-id"}, description = "Head job managed identity resource ID. Full Azure resource ID of the user-assigned managed identity for the head job. Required in Forge mode when head job managed identity client ID is specified. Format: /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{name}")
        public String managedIdentityHeadResourceId;

        @Option(names = {"--managed-identity-pool-resource-id"}, description = "Compute job managed identity resource ID. Full Azure resource ID of the user-assigned managed identity for compute jobs. Required in Forge mode when compute job managed identity client ID is specified. Format: /subscriptions/{sub}/resourceGroups/{rg}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/{name}")
        public String managedIdentityPoolResourceId;

    }

    public static class AdvancedOptions {

        @Option(names = {"--delete-jobs-on-completion"}, description = "Delete Azure Batch jobs when the workflow completes successfully. Failed jobs are always preserved. Default: false.")
        public Boolean deleteJobsOnCompletion;

        @Option(names = {"--delete-tasks-on-completion"}, description = "Delete individual Azure Batch tasks when they complete successfully. Failed tasks are preserved. Default: true.")
        public Boolean deleteTasksOnCompletion;

        @Option(names = {"--terminate-jobs-on-completion"}, description = "Terminate Azure Batch jobs when all tasks complete. Default: true.")
        public Boolean terminateJobsOnCompletion;

        @Option(names = {"--token-duration"}, description = "Duration of the SAS (shared access signature) token for Azure Blob Storage access. If absent, Platform defaults to 12h.")
        public String tokenDuration;

        @Option(names = {"--job-max-wall-clock-time"}, description = "Maximum elapsed time for an Azure Batch job before automatic termination. Accepts duration syntax (e.g., '7d', '1d12h', '168h'). Defaults to 7d. Maximum: 180 days.")
        public String jobMaxWallClockTime;

    }
}
