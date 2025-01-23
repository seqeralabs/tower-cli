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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AwsBatchManualPlatform extends AbstractPlatform<AwsBatchConfig> {

    @Option(names = {"-r", "--region"}, description = "AWS region.", required = true)
    public String region;

    @Option(names = {"--head-queue"}, description = "The Batch queue that will run the Nextflow application. A queue that does not use spot instances is expected.", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "The default Batch queue to which Nextflow will submit jobs. This can be overwritten via Nextflow config.", required = true)
    public String computeQueue;

    @Option(names = {"--fusion-v2"}, description = "With Fusion v2 enabled, S3 buckets specified in the Pipeline work directory and Allowed S3 Buckets fields will be accessible in the compute nodes storage (requires Wave containers service).")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Allow access to private container repositories and the provisioning of containers in your Nextflow pipelines via the Wave containers service.")
    public boolean wave;

    @Option(names = {"--fast-storage"}, description = "Allow the use of NVMe instance storage to speed up I/O and disk access operations (requires Fusion v2).")
    public boolean fastStorage;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AwsBatchManualPlatform() {
        super(PlatformEnum.AWS_BATCH);
    }

    /**
     * Clean backend generated config
     *
     * @param config
     */
    public static void clean(AwsBatchConfig config) {
        config.volumes(null);
    }

    @Override
    public AwsBatchConfig computeConfig() throws IOException, ApiException {
        return new AwsBatchConfig()
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .environment(environmentVariables())
                .fusion2Enabled(isFusionV2Enabled())
                .waveEnabled(wave)
                .nvnmeStorageEnabled(fastStorage)

                .region(region)

                // Queues
                .headQueue(headQueue)
                .computeQueue(computeQueue)

                // Advanced
                .cliPath(adv().cliPath)
                .computeJobRole(adv().computeJobRole)
                .headJobCpus(adv().headJobCpus)
                .headJobMemoryMb(adv().headJobMemoryMb)
                .headJobRole(adv().headJobRole);
    }

    private Boolean isFusionV2Enabled() throws TowerException {
        // TODO: delete this validation once wave is no longer a requirement for Fusion V2
        if (fusionV2 && !wave) throw new TowerException("Fusion v2 requires Wave service");
        return fusionV2;
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {
        @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job.")
        public Integer headJobMemoryMb;

        @Option(names = {"--head-job-role"}, description = "IAM role to fine-grained control permissions for the Nextflow runner job.")
        public String headJobRole;

        @Option(names = {"--compute-job-role"}, description = "IAM role to fine-grained control permissions for jobs submitted by Nextflow.")
        public String computeJobRole;

        @Option(names = {"--cli-path"}, description = "Nextflow requires the AWS CLI installed in the Ec2 instances. Use this field to specify the path.")
        public String cliPath;
    }
}
