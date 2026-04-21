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
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzBatchManualPlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an Azure Blob Storage path.", required = true)
    public String workDir;

    @Option(names = {"-l", "--location"}, description = "Azure region where compute resources will be deployed (e.g., eastus, westeurope).", required = true)
    public String location;

    @Option(names = {"--compute-pool-name"}, description = "Pre-configured Azure Batch pool for the Nextflow head job. When used with --worker-pool, this pool handles only the launcher. Must include azcopy command-line tool.", required = true)
    public String computePoolName;

    @Option(names = {"--worker-pool"}, description = "Pre-configured Azure Batch pool for pipeline worker tasks. When specified, the head job runs on --compute-pool-name and worker tasks run on this pool. Must be different from the head pool.")
    public String workerPool;

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

    public AzBatchManualPlatform() {
        super(PlatformEnum.AZURE_BATCH);
    }

    @Override
    public AzBatchConfig computeConfig() throws ApiException, IOException {
        AzBatchConfig config = new AzBatchConfig();

        config
                .fusion2Enabled(fusionV2)
                .waveEnabled(wave)
                .region(location)
                .headPool(computePoolName)
                .workerPool(workerPool);

        if (adv != null) {
            config
                    .deleteJobsOnCompletionEnabled(adv.deleteJobsOnCompletion)
                    .deleteTasksOnCompletion(adv.deleteTasksOnCompletion)
                    .terminateJobsOnCompletion(adv.terminateJobsOnCompletion)
                    .tokenDuration(adv.tokenDuration)
                    .jobMaxWallClockTime(adv.jobMaxWallClockTime);
        }

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        config.subnetId(subnetId);

        if (managedIdentity != null) {
            config.managedIdentityClientId(managedIdentity.managedIdentityHeadClientId);
            config.managedIdentityPoolClientId(managedIdentity.managedIdentityPoolClientId);
        }

        return config;
    }

    public static class ManagedIdentityOptions {

        @Option(names = {"--managed-identity-head-client-id"}, description = "Head job managed identity client ID (UUID). The user-assigned managed identity used by the Nextflow launcher (head job).")
        public String managedIdentityHeadClientId;

        @Option(names = {"--managed-identity-pool-client-id"}, description = "Compute job managed identity client ID (UUID). The user-assigned managed identity used by compute tasks running on Batch pool nodes.")
        public String managedIdentityPoolClientId;

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
