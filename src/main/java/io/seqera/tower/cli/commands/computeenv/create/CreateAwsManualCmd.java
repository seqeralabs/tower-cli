package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.AwsBatchManualPlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "aws-manual",
        description = "Create new AWS Batch compute environment using an existing environment"
)
public class CreateAwsManualCmd extends AbstractCreateCmd {

    @Mixin
    public AwsBatchManualPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
