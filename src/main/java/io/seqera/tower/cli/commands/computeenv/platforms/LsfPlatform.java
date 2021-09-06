package io.seqera.tower.cli.commands.computeenv.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.LsfComputeConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class LsfPlatform extends AbstractPlatform<LsfComputeConfig> {

    @Option(names = {"-u", "--user-name"}, description = "The username on the cluster used to launch the pipeline execution", required = true)
    public String userName;

    @Option(names = {"-H", "--host-name"}, description = "The pipeline execution is launched by connecting via SSH to the hostname specified. This usually is the cluster login node. Local IP addresses e.g. 127.*, 172.*, 192.*, etc. are not allowed, use a fully qualified hostname instead", required = true)
    public String hostName;

    @Option(names = {"-p", "--port"}, description = "Port number for the login connection.")
    public Integer port;

    @Option(names = {"-q", "--head-queue"}, description = "The name of the queue on the cluster used to launch the execution of the Nextflow pipeline", required = true)
    public String headQueue;

    @Option(names = {"--compute-queue"}, description = "The name of queue on the cluster to which pipeline jobs are submitted. This queue can be overridden by the pipeline configuration.")
    public String computeQueue;

    @Option(names = {"--launch-dir"}, description = "The directory where Nextflow runs. It must be an absolute directory and the user should have read-write access permissions to it. If omitted defaults to the pipeline work directory")
    public String launchDir;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public static class AdvancedOptions {
        @Option(names = {"--max-queue-size"}, description = "This option limits the number of jobs Nextflow can submit to the Slurm queue at the same time (default: 100)")
        public Integer maxQueueSize;

        @Option(names = {"--head-job-options"}, description = "Slurm submit options for the Nextflow head job. These options are added to the 'sbatch' command run by Tower to launch the pipeline execution.")
        public String headJobOptions;

        @Option(names = {"--unit-for-limits"}, description = "This option defines the unit used by your LSF cluster for memory limits. It should match the attribute LSF_UNIT_FOR_LIMITS setting in your lsf.conf file.")
        public String unitForLimits;

        @Option(names = {"--per-job-mem-limit"}, description = "Whether the memory limit is interpreted as per-job or per-process. It should match the attribute LSB_JOB_MEMLIMIT in your lsf.conf file")
        public Boolean perJobMemLimit;

        @Option(names = {"--per-task-reserve"}, description = "Whether the memory reservation is made on job tasks instead of per-host. It should match the attribute RESOURCE_RESERVE_PER_TASK in your lsf.conf file.")
        public Boolean perTaskReserve;

    }


    public LsfPlatform() {
        super(PlatformEnum.LSF_PLATFORM);
    }

    @Override
    public LsfComputeConfig computeConfig() throws ApiException, IOException {
        LsfComputeConfig config = new LsfComputeConfig();

        config
                // Common
                .platform(PlatformEnum.SLURM_PLATFORM.getValue())
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())

                // Main
                .launchDir(launchDir)
                .userName(userName)
                .hostName(hostName)
                .port(port)
                .headQueue(headQueue)
                .computeQueue(computeQueue);

        // Advanced
        config
                .unitForLimits(adv().unitForLimits)
                .perJobMemLimit(adv().perJobMemLimit)
                .perTaskReserve(adv().perTaskReserve)
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
}
