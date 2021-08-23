package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractCmd;
import io.seqera.tower.cli.commands.credentials.CreateCmd;
import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.model.CreateCredentialsRequest;
import io.seqera.tower.model.CredentialsSpec;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command
public abstract class AbstractCreateCmd extends AbstractCmd {

    @ParentCommand
    protected CreateCmd parent;

    @Option(names = {"-n", "--name"}, required = true)
    public String name;

    @Override
    public Tower app() {
        return parent.app();
    }

    @Override
    protected Integer exec() throws ApiException, IOException {

        AbstractProvider provider = getProvider();
        api().createCredentials(
                new CreateCredentialsRequest().credentials(
                        new CredentialsSpec()
                                .name(name)
                                .baseUrl(provider.baseUrl())
                                .provider(provider.type())
                                .keys(provider.securityKeys())
                ),
                workspaceId()
        );

        app().println(String.format("New %S credentials '%s' added at %s workspace", provider.type(), name, workspaceRef()));
        return 0;
    }

    protected abstract AbstractProvider getProvider();
}
