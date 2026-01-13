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
import io.seqera.tower.api.CredentialsApi;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.AzBatchForgeConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.JobCleanupPolicy;
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

    @Option(names = {"--vm-type"}, description = "Azure VM size for compute pool. Must be a valid Azure Batch VM type. Default: Standard_D4_v3.")
    public String vmType;

    @Option(names = {"--vm-count"}, description = "Number of VMs in the Batch pool. With autoscaling enabled, this is the maximum capacity. Pool scales to zero when unused.", required = true)
    public Integer vmCount;

    @Option(names = {"--no-auto-scale"}, description = "Disable pool autoscaling. When disabled, pool maintains fixed VM count and does not scale based on workload.")
    public boolean noAutoScale;

    @Option(names = {"--preserve-resources"}, description = "Preserve Azure Batch pool resources on deletion. Keeps the compute pool and related resources when the compute environment is deleted from Seqera Platform.")
    public boolean preserveResources;

    @Option(names = {"--registry-credentials"}, split = ",", paramLabel = "<credential_name>", description = "Container registry credentials for private registries. Comma-separated list of credential names to access private Docker registries.")
    public List<String> registryCredentials;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to Azure Blob Storage with low-latency I/O. Requires Wave containers. Default: false.")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Enable Wave containers. Allows access to private container repositories and on-demand container provisioning. Default: false.")
    public boolean wave;

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
                    .deleteJobsOnCompletion(adv.jobsCleanup)
                    .tokenDuration(adv.tokenDuration);
        }


        AzBatchForgeConfig forge = new AzBatchForgeConfig()
                .vmType(vmType)
                .vmCount(vmCount)
                .autoScale(!noAutoScale)
                .disposeOnDeletion(!preserveResources);

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

        return config;
    }

    public static class AdvancedOptions {

        @Option(names = {"--jobs-cleanup"}, description = "Automatic deletion of Azure Batch jobs after pipeline execution. ON_SUCCESS deletes only successful jobs. ALWAYS deletes all jobs. NEVER keeps all jobs.")
        public JobCleanupPolicy jobsCleanup;

        @Option(names = {"--token-duration"}, description = "Duration of the SAS (shared access signature) token for Azure Blob Storage access. Default: 12h.")
        public String tokenDuration;

    }
}
