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

import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AwsBatchManualPlatform extends AbstractPlatform<AwsBatchConfig> {

    @Option(names = {"-r", "--region"}, description = "AWS region", required = true)
    public String region;

    @Option(names = {"--head-queue"}, description = "The Batch queue that will run the Nextflow application. A queue that does not use spot instances is expected", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "The default Batch queue to which Nextflow will submit jobs. This can be overwritten via Nextflow config", required = true)
    public String computeQueue;

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
    public AwsBatchConfig computeConfig() throws IOException {
        return new AwsBatchConfig()
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
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

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {
        @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job")
        public Integer headJobMemoryMb;

        @Option(names = {"--head-job-role"}, description = "IAM role to fine-grained control permissions for the Nextflow runner job")
        public String headJobRole;

        @Option(names = {"--compute-job-role"}, description = "IAM role to fine-grained control permissions for jobs submitted by Nextflow")
        public String computeJobRole;

        @Option(names = {"--cli-path"}, description = "Nextflow requires the AWS CLI installed in the Ec2 instances. Use this field to specify the path")
        public String cliPath;
    }
}
