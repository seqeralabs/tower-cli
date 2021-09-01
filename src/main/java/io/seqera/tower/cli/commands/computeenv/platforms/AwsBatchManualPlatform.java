package io.seqera.tower.cli.commands.computeenv.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.ForgeConfig;
import io.seqera.tower.model.ForgeConfig.AllocStrategyEnum;
import io.seqera.tower.model.ForgeConfig.TypeEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;

public class AwsBatchManualPlatform extends AbstractPlatform<AwsBatchConfig> {

    @Option(names = {"--region"}, description = "AWS region", required = true)
    public String region;

    @Option(names = {"--head-queue"}, description = "The Batch queue that will run the Nextflow application. A queue that does not use spot instances is expected.", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "The default Batch queue to which Nextflow will submit job executions. This can be overwritten via the usual Nextflow config.", required = true)
    public String computeQueue;

    @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job")
    public Integer headJobCpus;

    @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job")
    public Integer headJobMemoryMb;

    @Option(names = {"--head-job-role"}, description = "IAM role to fine-grained control permissions for the Nextflow runner job")
    public String headJobRole;

    @Option(names = {"--compute-job-role"}, description = "IAM role to fine-grained control permissions for jobs submitted by Nextflow")
    public String computeJobRole;

    @Option(names = {"--cli-path"}, description = "Nextflow requires the AWS CLI tool to be installed in the Ec2 instances launched by Batch. Use this field to specify the path where the tool is located. It must start with a '/' and terminate with the '/bin/aws' suffix (default: '/home/ec2-user/miniconda/bin/aws')")
    public String cliPath;

    public AwsBatchManualPlatform() {
        super(PlatformEnum.AWS_BATCH);
    }

    @Override
    public AwsBatchConfig computeConfig() throws ApiException, IOException {
        return new AwsBatchConfig()
                .platform(PlatformEnum.AWS_BATCH.getValue())
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .region(region)

                // Queues
                .headQueue(headQueue)
                .computeQueue(computeQueue)

                // Advanced
                .cliPath(cliPath)
                .computeJobRole(computeJobRole)
                .headJobCpus(headJobCpus)
                .headJobMemoryMb(headJobMemoryMb)
                .headJobRole(headJobRole);
    }

    /**
     * Clean backend generated config
     *
     * @param config
     */
    public static void clean(AwsBatchConfig config) {
        config.volumes(null);
    }
}
