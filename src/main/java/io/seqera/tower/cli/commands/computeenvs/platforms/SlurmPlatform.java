/*
 * Copyright 2023, Seqera.
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
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.SlurmComputeConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class SlurmPlatform extends AbstractPlatform<SlurmComputeConfig> {

    @Option(names = {"-u", "--user-name"}, description = "The username on the cluster used to launch the pipeline execution.")
    public String userName;

    @Option(names = {"-H", "--host-name"}, description = "The pipeline execution is launched by connecting via SSH to the hostname specified. This usually is the cluster login node. Local IP addresses e.g. 127.*, 172.*, 192.*, etc. are not allowed, use a fully qualified hostname instead.")
    public String hostName;

    @Option(names = {"-p", "--port"}, description = "Port number for the login connection.")
    public Integer port;

    @Option(names = {"-q", "--head-queue"}, description = "The name of the queue on the cluster used to launch the execution of the Nextflow pipeline.", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "The name of queue on the cluster to which pipeline jobs are submitted. This queue can be overridden by the pipeline configuration.")
    public String computeQueue;

    @Option(names = {"--launch-dir"}, description = "The directory where Nextflow runs. It must be an absolute directory and the user should have read-write access permissions to it [default: pipeline work directory].")
    public String launchDir;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public SlurmPlatform() {
        super(PlatformEnum.SLURM_PLATFORM);
    }

    @Override
    public SlurmComputeConfig computeConfig() throws ApiException, IOException {
        SlurmComputeConfig config = new SlurmComputeConfig();

        config
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .environment(environmentVariables())

                // Main
                .launchDir(launchDir)
                .userName(userName)
                .hostName(hostName)
                .port(port)
                .headQueue(headQueue)
                .computeQueue(computeQueue)

                // Advanced
                .maxQueueSize(adv().maxQueueSize)
                .headJobOptions(adv().headJobOptions);

        return config;
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {
        @Option(names = {"--max-queue-size"}, description = "This option limits the number of jobs Nextflow can submit to the Slurm queue at the same time [default: 100].")
        public Integer maxQueueSize;

        @Option(names = {"--head-job-options"}, description = "Slurm submit options for the Nextflow head job. These options are added to the 'sbatch' command run by Tower to launch the pipeline execution.")
        public String headJobOptions;
    }
}
