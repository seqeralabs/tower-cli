package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractCmd;
import io.seqera.tower.cli.commands.credentials.UpdateCmd;
import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.responses.CredentialsUpdated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.CredentialsSpec;
import io.seqera.tower.model.DescribeCredentialsResponse;
import io.seqera.tower.model.UpdateCredentialsRequest;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command
public abstract class AbstractUpdateCmd extends AbstractCmd {

    @ParentCommand
    protected UpdateCmd parent;

    @CommandLine.Option(names = {"-i", "--id"}, required = true)
    public String id;

    public AbstractUpdateCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
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
                throw new ApiException(403, String.format("Unknown credentials with id '%s' at %s workspace", id, workspaceRef()));
            }
            throw e;
        }
    }

    protected Response update(@NotNull Credentials creds) throws ApiException, IOException {

        //TODO do we want to allow to change the name? name must be unique at workspace level?
        String name = creds.getName();

        AbstractProvider provider = getProvider();
        api().updateCredentials(id,
                new UpdateCredentialsRequest().credentials(
                        new CredentialsSpec()
                                .id(id)
                                .name(name)
                                .baseUrl(provider.baseUrl())
                                .provider(provider.type())
                                .keys(provider.securityKeys())
                ),
                workspaceId()
        );

        return new CredentialsUpdated(provider.type().name(), name, workspaceRef());
    }

    protected abstract AbstractProvider getProvider();

}


