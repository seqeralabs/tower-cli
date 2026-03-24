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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AwsBatchManualPlatform extends AbstractPlatform<AwsBatchConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be an S3 bucket path (e.g., s3://your-bucket/work).", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "AWS region where compute resources will be created (e.g., us-east-1, eu-west-1).", required = true)
    public String region;

    @Option(names = {"--head-queue"}, description = "AWS Batch queue for the Nextflow head job. Should use on-demand instances for reliability.", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "AWS Batch compute queue for running pipeline jobs. Nextflow submits tasks to this queue. Can be overridden in pipeline config.", required = true)
    public String computeQueue;

    @Option(names = {"--fusion-v2"}, description = "Enable Fusion file system. Provides native access to S3 storage with low-latency I/O. Requires Wave containers.")
    public boolean fusionV2;

    @Option(names = {"--wave"}, description = "Enable Wave containers. Allows access to private container repositories and on-demand container provisioning.")
    public boolean wave;

    @Option(names = {"--fast-storage"}, description = "Enable NVMe instance storage. Provides high-performance local storage for faster I/O operations. Requires Fusion file system.")
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
        AwsBatchConfig config = new AwsBatchConfig()
                .region(region)
                .fusion2Enabled(isFusionV2Enabled())
                .waveEnabled(wave)
                .nvnmeStorageEnabled(fastStorage)

                // Queues
                .headQueue(headQueue)
                .computeQueue(computeQueue)

                // Advanced
                .cliPath(adv().cliPath)
                .executionRole(adv().batchExecutionRole)
                .computeJobRole(adv().computeJobRole)
                .headJobCpus(adv().headJobCpus)
                .headJobMemoryMb(adv().headJobMemoryMb)
                .headJobRole(adv().headJobRole);

                // Common
                config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

                return config;
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
        @Option(names = {"--head-job-cpus"}, description = "Number of CPUs allocated to the Nextflow head job. Controls the compute resources for the main workflow orchestration process.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "Memory allocation for the Nextflow head job in megabytes. Determines available memory for workflow orchestration.")
        public Integer headJobMemoryMb;

        @Option(names = {"--head-job-role"}, description = "IAM role ARN to grant fine-grained permissions to the Nextflow head job. Enables secure access to AWS resources.")
        public String headJobRole;

        @Option(names = {"--compute-job-role"}, description = "IAM role ARN to grant fine-grained permissions to Nextflow compute jobs. Controls access for individual pipeline tasks.")
        public String computeJobRole;

        @Option(names = {"--batch-execution-role"}, description = "IAM role ARN for ECS task execution. Grants Amazon ECS containers permission to make AWS API calls on your behalf.")
        public String batchExecutionRole;

        @Option(names = {"--cli-path"}, description = "Nextflow requires the AWS CLI installed in the Ec2 instances. Use this field to specify the path.")
        public String cliPath;
    }
}
