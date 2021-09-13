package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.AzBatchForgeConfig;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.JobCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzBatchForgePlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"-l", "--location"}, description = "The Azure location where the workload will be deployed", required = true)
    public String location;

    @Option(names = {"--vm-type"}, description = "Specify the virtual machine type used by this pool. It must be a valid Azure Batch VM type (default: Standard_D4_v3)")
    public String vmType;

    @Option(names = {"--vm-count"}, description = "The number of virtual machines in this pool. When autoscaling feature is enabled, this option represents the maximum number of virtual machines to which the pool can grow and automatically scales to zero when unused")
    public Integer vmCount;

    @Option(names = {"--no-auto-scale"}, description = "Disable pool autoscaling which automatically adjust the pool size depending the number submitted jobs and scale to zero when the pool is unused")
    public boolean noAutoScale;

    @Option(names = {"--preserve-resources"}, description = "Enable this if you want to preserve the Batch compute pool created by Tower independently from the lifecycle of this compute environment")
    public boolean preserveResources;


    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public static class AdvancedOptions {

        @Option(names = {"--jobs-cleanup"}, description = "Enable the automatic deletion of Batch jobs created by the pipeline execution (ON_SUCESS, ALWAYS, NEVER)")
        public JobCleanupPolicy jobsCleanup;

        @Option(names = {"--token-duration"}, description = "The duration of the shared access signature token created by Nextflow when the 'sasToken' option is not specified (default: 12h)")
        public String tokenDuration;

    }

    public AzBatchForgePlatform() {
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
                .region(location);

        if (adv != null) {
            config
                    .deleteJobsOnCompletion(adv.jobsCleanup)
                    .tokenDuration(adv.tokenDuration);
        }


        config.forge(new AzBatchForgeConfig()
                .vmType(vmType)
                .vmCount(vmCount)
                .autoScale(!noAutoScale)
                .disposeOnDeletion(!preserveResources)
        );

        return config;
    }
}
