package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import io.seqera.tower.cli.commands.computeenv.platforms.UnivaPlatform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "uge",
        description = "Create new UNIVA grid engine compute environment"
)
public class CreateUgeCmd extends AbstractCreateCmd {

    @Mixin
    public UnivaPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
