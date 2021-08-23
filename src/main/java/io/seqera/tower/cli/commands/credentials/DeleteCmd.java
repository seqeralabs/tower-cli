package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
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
    protected Integer exec() throws ApiException, IOException {
        try {
            api().deleteCredentials(id, workspaceId());
            app().println(String.format("AWS credentials '%s' deleted at %s workspace", id, workspaceRef()));
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ApiException(403, String.format("Unknown credentials with id '%s' at %s workspace", id, workspaceRef()));
            }
            throw e;
        }
        return 0;
    }
}
