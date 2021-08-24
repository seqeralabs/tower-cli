package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.CredentialsDeleted;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "delete",
        description = "Delete workspace credentials"
)
public class DeleteCmd extends AbstractCredentialsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException {
        try {
            api().deleteCredentials(id, workspaceId());
            return new CredentialsDeleted(id, workspaceRef());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ApiException(403, String.format("Unknown credentials with id '%s' at %s workspace", id, workspaceRef()));
            }
            throw e;
        }
    }
}
