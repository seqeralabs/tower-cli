package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.credentials.providers.K8sProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "k8s",
        description = "Create new Kubernetes workspace credentials"
)
public class CreateK8sCmd extends AbstractCreateCmd {

    @Mixin
    K8sProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
