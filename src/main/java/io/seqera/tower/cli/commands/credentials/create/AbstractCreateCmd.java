package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.credentials.CreateCmd;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.responses.CredentialsCreated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CreateCredentialsRequest;
import io.seqera.tower.model.CreateCredentialsResponse;
import io.seqera.tower.model.CredentialsSpec;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command
public abstract class AbstractCreateCmd<T extends SecurityKeys> extends AbstractApiCmd {

    @Option(names = {"-n", "--name"}, description = "Credentials name", required = true)
    public String name;
    @ParentCommand
    protected CreateCmd parent;

    @Override
    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        CredentialsSpec specs = new CredentialsSpec();
        specs
                .keys(getProvider().securityKeys())
                .name(name)
                .baseUrl(getProvider().baseUrl())
                .provider(getProvider().type());

        CreateCredentialsResponse resp = api().createCredentials(new CreateCredentialsRequest().credentials(specs), workspaceId());

        return new CredentialsCreated(getProvider().type().name(), resp.getCredentialsId(), name, workspaceRef());
    }

    protected abstract CredentialsProvider getProvider();
}
