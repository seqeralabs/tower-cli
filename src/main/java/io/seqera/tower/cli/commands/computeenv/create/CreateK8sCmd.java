package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.AbstractPlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.K8sPlatform;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Command;

@Command(
    name = "k8s",
    description = "Create new Kubernetes compute environment"
)
public class CreateK8sCmd extends AbstractCreateCmd {

    @Mixin
    public K8sPlatform platform;

    @Override
    protected AbstractPlatform getPlatform() {
        return platform;
    }
}
