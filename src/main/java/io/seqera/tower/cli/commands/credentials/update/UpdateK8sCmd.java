package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.K8sProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "k8s",
        description = "Update Kubernetes workspace credentials"
)
public class UpdateK8sCmd extends AbstractUpdateCmd {

    @Mixin
    protected K8sProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
