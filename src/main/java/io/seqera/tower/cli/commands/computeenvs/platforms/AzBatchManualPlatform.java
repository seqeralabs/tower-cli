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
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.JobCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzBatchManualPlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"-l", "--location"}, description = "The Azure location where the workload will be deployed.", required = true)
    public String location;

    @Option(names = {"--compute-pool-name"}, description = "The Azure Batch compute pool to be used to run the Nextflow jobs. This needs to be a pre-configured Batch compute pool which includes the azcopy command line (see the Tower documentation for details).", required = true)
    public String computePoolName;

    @Option(names = {"--fusion-v2"}, description = "With Fusion v2 enabled, Azure blob containers specified in the pipeline work directory and blob containers within the Azure storage account will be accessible in the compute nodes storage (requires Wave containers service).")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Allow access to private container repositories and the provisioning of containers in your Nextflow pipelines via the Wave containers service.")
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
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables())
                .fusion2Enabled(fusionV2)
                .waveEnabled(wave)
                .region(location)
                .headPool(computePoolName);

        if (adv != null) {
            config
                    .deleteJobsOnCompletion(adv.jobsCleanup)
                    .tokenDuration(adv.tokenDuration);
        }

        return config;
    }

    public static class AdvancedOptions {

        @Option(names = {"--jobs-cleanup"}, description = "Enable the automatic deletion of Batch jobs created by the pipeline execution (ON_SUCCESS, ALWAYS, NEVER).")
        public JobCleanupPolicy jobsCleanup;

        @Option(names = {"--token-duration"}, description = "The duration of the shared access signature token created by Nextflow when the 'sasToken' option is not specified [default: 12h].")
        public String tokenDuration;

    }
}
