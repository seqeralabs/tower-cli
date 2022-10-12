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
import io.seqera.tower.model.GoogleBatchConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GoogleBatchPlatform extends AbstractPlatform<GoogleBatchConfig> {

    @Option(names = {"--location"}, description = "The location where the job executions are deployed to Google Batch API.", required = true)
    public String location;

    @Option(names = {"--spot"}, description = "Use Spot virtual machines.")
    public Boolean spot;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public GoogleBatchPlatform() {
        super(PlatformEnum.GOOGLE_LIFESCIENCES);
    }

    @Override
    public GoogleBatchConfig computeConfig() throws ApiException, IOException {
        GoogleBatchConfig config = new GoogleBatchConfig();

        config
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())

                // Main
                .location(location)
                .spot(spot);

        // Advanced
        if (adv != null) {
            config
                    .usePrivateAddress(adv.usePrivateAddress)
                    .bootDiskSizeGb(adv.bootDiskSizeGb)
                    .headJobCpus(adv.headJobCpus)
                    .headJobMemoryMb(adv.headJobMemoryMb);
        }

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
    }
}
