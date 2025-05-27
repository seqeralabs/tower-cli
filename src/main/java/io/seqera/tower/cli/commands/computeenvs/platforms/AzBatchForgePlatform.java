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

    @Option(names = {"-l", "--location"}, description = "The Azure location where the workload will be deployed.", required = true)
    public String location;

    @Option(names = {"--vm-type"}, description = "Specify the virtual machine type used by this pool. It must be a valid Azure Batch VM type [default: Standard_D4_v3].")
    public String vmType;

    @Option(names = {"--vm-count"}, description = "The number of virtual machines in this pool. When autoscaling feature is enabled, this option represents the maximum number of virtual machines to which the pool can grow and automatically scales to zero when unused.", required = true)
    public Integer vmCount;

    @Option(names = {"--no-auto-scale"}, description = "Disable pool autoscaling which automatically adjust the pool size depending the number submitted jobs and scale to zero when the pool is unused.")
    public boolean noAutoScale;

    @Option(names = {"--preserve-resources"}, description = "Enable this if you want to preserve the Batch compute pool created by Tower independently from the lifecycle of this compute environment.")
    public boolean preserveResources;

    @Option(names = {"--registry-credentials"}, split = ",", paramLabel = "<credential_name>", description = "Comma-separated list of container registry credentials name.")
    public List<String> registryCredentials;

    @Option(names = {"--fusion-v2"}, description = "With Fusion v2 enabled, Azure blob containers specified in the pipeline work directory and blob containers within the Azure storage account will be accessible in the compute nodes storage (requires Wave containers service).")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Allow access to private container repositories and the provisioning of containers in your Nextflow pipelines via the Wave containers service.")
    public boolean wave;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AzBatchForgePlatform() {
        super(PlatformEnum.AZURE_BATCH);
    }

    @Override
    public AzBatchConfig computeConfig(Long workspaceId, CredentialsApi credentialsApi) throws ApiException, IOException {
        AzBatchConfig config = new AzBatchConfig();

        config
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .environment(environmentVariables())
                .fusion2Enabled(fusionV2)
                .waveEnabled(wave)
                .region(location);

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

        @Option(names = {"--jobs-cleanup"}, description = "Enable the automatic deletion of Batch jobs created by the pipeline execution (ON_SUCCESS, ALWAYS, NEVER).")
        public JobCleanupPolicy jobsCleanup;

        @Option(names = {"--token-duration"}, description = "The duration of the shared access signature token created by Nextflow when the 'sasToken' option is not specified [default: 12h].")
        public String tokenDuration;

    }
}
