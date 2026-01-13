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
import io.seqera.tower.model.GoogleLifeSciencesConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;

public class GoogleLifeSciencesPlatform extends AbstractPlatform<GoogleLifeSciencesConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored. Must be a Google Cloud Storage bucket path (e.g., gs://your-bucket/work).", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "Google Cloud region where the workload will be executed (e.g., us-central1, europe-west1).", required = true)
    public String region;

    @Option(names = {"--zones"}, split = ",", description = "Google Cloud zones for workload execution. Comma-separated list. If specified, takes priority over region setting.")
    public List<String> zones;

    @Option(names = {"--location"}, description = "Location where job executions are deployed to Cloud Life Sciences API. Default: same as specified region/zone.")
    public String location;

    @Option(names = {"--preemptible"}, description = "Use preemptible virtual machines. Enables cost-effective instances for compute workloads. Preemptible VMs may be interrupted when capacity is needed.")
    public Boolean preemptible;

    @ArgGroup(heading = "%nFilestore file system:%n", validate = false)
    public Filestore filestore;
    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public GoogleLifeSciencesPlatform() {
        super(PlatformEnum.GOOGLE_LIFESCIENCES);
    }

    @Override
    public GoogleLifeSciencesConfig computeConfig() throws ApiException, IOException {
        GoogleLifeSciencesConfig config = new GoogleLifeSciencesConfig();

        config
                // Main
                .region(region)
                .zones(zones)
                .location(location)
                .preemptible(preemptible);

        // Advanced
        if (adv != null) {
            config
                    .usePrivateAddress(adv.usePrivateAddress)
                    .bootDiskSizeGb(adv.bootDiskSizeGb)
                    .headJobCpus(adv.headJobCpus)
                    .headJobMemoryMb(adv.headJobMemoryMb);
        }

        if (filestore != null) {
            config
                    .nfsMount(filestore.nfsMount)
                    .nfsTarget(filestore.nfsTarget);

        }

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    public static class Filestore {

        @Option(names = {"--nfs-target"}, description = "Google Cloud Filestore instance IP address and share name. Format: IP:SHARE_NAME (e.g., 10.0.0.1:/my_share).")
        public String nfsTarget;

        @Option(names = {"--nfs-mount"}, description = "NFS mount path for the Filestore volume. Should be the same as or a parent directory of the pipeline work directory. Default: pipeline work directory.")
        public String nfsMount;

    }

    public static class AdvancedOptions {
        @Option(names = {"--use-private-address"}, description = "Do not attach a public IP address to VM instances. When enabled, only Google internal services are accessible. Requires Cloud NAT for external access.")
        public Boolean usePrivateAddress;

        @Option(names = {"--boot-disk-size"}, description = "Boot disk size in GB. Controls the root volume size for compute instances. Default: 50 GB.")
        public Integer bootDiskSizeGb;

        @Option(names = {"--head-job-cpus"}, description = "Number of CPUs allocated to the Nextflow head job. Controls the compute resources for the main workflow orchestration process.")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "Memory allocation for the Nextflow head job in megabytes. Value must be a multiple of 256 MiB and from 0.5 GB to 8 GB per CPU.")
        public Integer headJobMemoryMb;
    }
}
