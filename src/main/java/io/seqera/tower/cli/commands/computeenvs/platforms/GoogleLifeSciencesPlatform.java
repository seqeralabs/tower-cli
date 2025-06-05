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

    @Option(names = {"-r", "--region"}, description = "The region where the workload will be executed.", required = true)
    public String region;

    @Option(names = {"--zones"}, split = ",", description = "One or more zones where the workload will be executed. If specified, it has priority over the region setting.")
    public List<String> zones;

    @Option(names = {"--location"}, description = "The location where the job executions are deployed to Cloud Life Sciences API [default: same as the specified region/zone].")
    public String location;

    @Option(names = {"--preemptible"}, description = "Use preemptible virtual machines.")
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
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables())

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

        return config;
    }

    public static class Filestore {

        @Option(names = {"--nfs-target"}, description = "The Filestore instance IP address and share file name e.g. 1.2.3.4:/my_share_name.")
        public String nfsTarget;

        @Option(names = {"--nfs-mount"}, description = "Specify the NFS mount path. It should be the same as the pipeline work directory or a parent path of it [default: pipeline work directory].")
        public String nfsMount;

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
    }
}
