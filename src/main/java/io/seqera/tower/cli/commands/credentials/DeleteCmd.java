package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.cli.responses.CredentialsDeleted;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "delete",
        description = "Delete workspace credentials"
)
public class DeleteCmd extends AbstractCredentialsCmd {

    @Option(names = {"-i", "--id"}, description = "Credentials identifier", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException {
        try {
            api().deleteCredentials(id, workspaceId());
            return new CredentialsDeleted(id, workspaceRef());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new CredentialsNotFoundException(id, workspaceRef());
            }
            throw e;
        }
    }
}
