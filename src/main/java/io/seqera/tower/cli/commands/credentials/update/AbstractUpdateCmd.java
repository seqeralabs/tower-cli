package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.cli.responses.CredentialsUpdated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.CredentialsSpec;
import io.seqera.tower.model.DescribeCredentialsResponse;
import io.seqera.tower.model.UpdateCredentialsRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command
public abstract class AbstractUpdateCmd extends AbstractApiCmd {

    @CommandLine.Option(names = {"-i", "--id"}, required = true)
    public String id;

    public AbstractUpdateCmd() {
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        // Check that exists
        try {
            DescribeCredentialsResponse response = api().describeCredentials(id, workspaceId());
            return update(response.getCredentials());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new CredentialsNotFoundException(id, workspaceRef());
            }
            throw e;
        }
    }

    protected Response update(Credentials creds) throws ApiException, IOException {

        //TODO do we want to allow to change the name? name must be unique at workspace level?
        String name = creds.getName();

        CredentialsSpec specs = new CredentialsSpec();
        specs
                .keys(getProvider().securityKeys())
                .name(name)
                .baseUrl(getProvider().baseUrl())
                .provider(getProvider().type())
                .id(id);

        api().updateCredentials(id, new UpdateCredentialsRequest().credentials(specs), workspaceId());

        return new CredentialsUpdated(getProvider().type().name(), name, workspaceRef());
    }

    protected abstract CredentialsProvider getProvider();

}


