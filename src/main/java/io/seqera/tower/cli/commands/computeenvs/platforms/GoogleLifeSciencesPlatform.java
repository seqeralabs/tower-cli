/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.GoogleLifeSciencesConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;

public class GoogleLifeSciencesPlatform extends AbstractPlatform<GoogleLifeSciencesConfig> {

    @Option(names = {"-r", "--region"}, description = "The region where the workload will be executed", required = true)
    public String region;

    @Option(names = {"--zones"}, split = ",", description = "One or more zones where the workload will be executed. If specified, it has priority over the region setting.")
    public List<String> zones;

    @Option(names = {"--location"}, description = "The location where the job executions are deployed to Cloud Life Sciences API (default: the same as the region or the zones specified)")
    public String location;

    @Option(names = {"--preemptible"}, description = "Use preemptible virtual machines")
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

        @Option(names = {"--nfs-target"}, description = "The Filestore instance IP address and share file name e.g. 1.2.3.4:/my_share_name")
        public String nfsTarget;

        @Option(names = {"--nfs-mount"}, description = "Specify the NFS mount path. It should be the same as the pipeline work directory or a parent path of it (defaults to the pipeline work directory root path if omitted).")
        public String nfsMount;

    }

    public static class AdvancedOptions {
        @Option(names = {"--use-private-address"}, description = "Do not attach a public IP address to the VM. When enabled only Google internal services are accessible.")
        public Boolean usePrivateAddress;

        @Option(names = {"--boot-disk-size"}, description = "Enter the boot disk size as GB")
        public Integer bootDiskSizeGb;

        @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job (value should be a multiple of 256MiB and from 0.5 GB to 8 GB per CPU)")
        public Integer headJobMemoryMb;
    }
}
