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
import io.seqera.tower.model.AltairPbsComputeConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AltairPlatform extends AbstractPlatform<AltairPbsComputeConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory on the cluster's shared file system. Must be an absolute path accessible from all compute nodes.", required = true)
    public String workDir;

    @Option(names = {"-u", "--user-name"}, description = "Username for SSH connection to the HPC cluster. Used to authenticate and launch pipeline execution on the head node.")
    public String userName;

    @Option(names = {"-H", "--host-name"}, description = "Hostname or IP address of the HPC head node for SSH connection. Typically the cluster login node. Must be a fully qualified hostname, not a local IP address.")
    public String hostName;

    @Option(names = {"-p", "--port"}, description = "SSH port for cluster connection. If absent, Platform defaults to port 22.")
    public Integer port;

    @Option(names = {"-q", "--head-queue"}, description = "Altair PBS queue for launching the Nextflow head job. The queue where the main workflow orchestration process runs.", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "Altair PBS queue for pipeline task submission. Nextflow submits individual jobs to this queue. Can be overridden in pipeline configuration.")
    public String computeQueue;

    @Option(names = {"--launch-dir"}, description = "Directory where Nextflow executes. Must be an absolute path with read-write permissions (if absent, Platform defaults to the pipeline work directory).")
    public String launchDir;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AltairPlatform() {
        super(PlatformEnum.ALTAIR_PLATFORM);
    }

    @Override
    public AltairPbsComputeConfig computeConfig() throws ApiException, IOException {
        AltairPbsComputeConfig config = new AltairPbsComputeConfig();

        config
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

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {
        @Option(names = {"--max-queue-size"}, description = "Maximum number of jobs Nextflow can submit simultaneously to the Altair PBS queue. Controls job submission rate. If absent, Platform defaults to 100.")
        public Integer maxQueueSize;

        @Option(names = {"--head-job-options"}, description = "Additional submit options for the Nextflow head job. Appended to the submit command for the main orchestration process.")
        public String headJobOptions;
    }
}
