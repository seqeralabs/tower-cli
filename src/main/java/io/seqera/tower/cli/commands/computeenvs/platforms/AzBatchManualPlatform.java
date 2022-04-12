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
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.JobCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzBatchManualPlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"-l", "--location"}, description = "The Azure location where the workload will be deployed.", required = true)
    public String location;

    @Option(names = {"--compute-pool-name"}, description = "The Azure Batch compute pool to be used to run the Nextflow jobs. This needs to be a pre-configured Batch compute pool which includes the azcopy command line (see the Tower documentation for details).", required = true)
    public String computePoolName;

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
