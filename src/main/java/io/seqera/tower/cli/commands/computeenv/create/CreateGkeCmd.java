package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.GkePlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "gke",
        description = "Create new Google GKE compute environment"
)
public class CreateGkeCmd extends AbstractCreateCmd {

    @Mixin
    public GkePlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
