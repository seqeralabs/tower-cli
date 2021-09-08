package io.seqera.tower.cli.commands.computeenvs.create;

import io.seqera.tower.cli.commands.computeenvs.platforms.EksPlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "eks",
        description = "Create new Amazon EKS compute environment"
)
public class CreateEksCmd extends AbstractCreateCmd {

    @Mixin
    public EksPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
