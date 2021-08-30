package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.K8sPlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "k8s",
        description = "Create new Kubernetes compute environment"
)
public class CreateK8sCmd extends AbstractCreateCmd {

    @Mixin
    public K8sPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
