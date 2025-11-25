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
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.GoogleBatchConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GoogleBatchPlatform extends AbstractPlatform<GoogleBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Work directory.", required = true)
    public String workDir;

    @Option(names = {"-l", "--location"}, description = "The location where the job executions are deployed to Google Batch API.", required = true)
    public String location;

    @Option(names = {"--spot"}, description = "Use Spot virtual machines.")
    public Boolean spot;

    @Option(names = {"--fusion-v2"}, description = "With Fusion v2 enabled, S3 buckets specified in the Pipeline work directory and Allowed S3 Buckets fields will be accessible in the compute nodes storage (requires Wave containers service).")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Allow access to private container repositories and the provisioning of containers in your Nextflow pipelines via the Wave containers service.")
    public boolean wave;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public GoogleBatchPlatform() {
        super(PlatformEnum.GOOGLE_BATCH);
    }

    @Override
    public GoogleBatchConfig computeConfig() throws ApiException, IOException {
        GoogleBatchConfig config = new GoogleBatchConfig();

        config
                .fusion2Enabled(fusionV2)
                .waveEnabled(wave)

                // Main
                .location(location)
                .spot(spot);

        // Advanced
        if (adv != null) {
            config
                .usePrivateAddress(adv.usePrivateAddress)
                .bootDiskSizeGb(adv.bootDiskSizeGb)
                .headJobCpus(adv.headJobCpus)
                .headJobMemoryMb(adv.headJobMemoryMb)
                .serviceAccount(adv.serviceAccountEmail);
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
        @Option(names = {"--use-private-address"}, description = "Do not attach a public IP address to the VM. When enabled only Google internal services are accessible.")
        public Boolean usePrivateAddress;

        @Option(names = {"--boot-disk-size"}, description = "Enter the boot disk size as GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job (value should be a multiple of 256MiB and from 0.5 GB to 8 GB per CPU).")
        public Integer headJobMemoryMb;

        @Option(names = {"--service-account-email"}, description = "The service account email address used when deploying pipeline executions with this compute environment.")
        public String serviceAccountEmail;

    }
}
