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
import io.seqera.tower.model.JobCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzBatchManualPlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an Azure Blob Storage path.", required = true)
    public String workDir;

    @Option(names = {"-l", "--location"}, description = "Azure region where compute resources will be deployed (e.g., eastus, westeurope).", required = true)
    public String location;

    @Option(names = {"--compute-pool-name"}, description = "Pre-configured Azure Batch compute pool for running Nextflow jobs. Must include azcopy command-line tool. See Nextflow documentation for pool requirements.", required = true)
    public String computePoolName;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to Azure Blob Storage with low-latency I/O. Requires Wave containers.")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Enable Wave containers. Allows access to private container repositories and on-demand container provisioning.")
    public boolean wave;

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
                .headPool(computePoolName);

        if (adv != null) {
            config
                    .deleteJobsOnCompletion(adv.jobsCleanup)
                    .tokenDuration(adv.tokenDuration);
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

        @Option(names = {"--jobs-cleanup"}, description = "Automatic deletion of Azure Batch jobs after pipeline execution. ON_SUCCESS deletes only successful jobs. ALWAYS deletes all jobs. NEVER keeps all jobs.")
        public JobCleanupPolicy jobsCleanup;

        @Option(names = {"--token-duration"}, description = "Duration of the SAS (shared access signature) token for Azure Blob Storage access. If absent, Platform defaults to 12h.")
        public String tokenDuration;

    }
}
