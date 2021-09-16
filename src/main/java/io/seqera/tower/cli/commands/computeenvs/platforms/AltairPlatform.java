package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.AltairPbsComputeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AltairPlatform extends AbstractPlatform<AltairPbsComputeConfig> {

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

    public AltairPlatform() {
        super(PlatformEnum.ALTAIR_PLATFORM);
    }

    @Override
    public AltairPbsComputeConfig computeConfig() throws ApiException, IOException {
        AltairPbsComputeConfig config = new AltairPbsComputeConfig();

        config
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())

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
        @Option(names = {"--max-queue-size"}, description = "This option limits the number of jobs Nextflow can submit to the Slurm queue at the same time (default: 100)")
        public Integer maxQueueSize;

        @Option(names = {"--head-job-options"}, description = "Slurm submit options for the Nextflow head job. These options are added to the 'sbatch' command run by Tower to launch the pipeline execution.")
        public String headJobOptions;
    }
}
