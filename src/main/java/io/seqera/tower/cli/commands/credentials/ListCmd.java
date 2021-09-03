package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.CredentialsList;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.ListCredentialsResponse;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List all workspace credentials"
)
public class ListCmd extends AbstractCredentialsCmd {

    @Override
    protected Response exec() throws ApiException, IOException {
        ListCredentialsResponse response = api().listCredentials(workspaceId(), null);
        return new CredentialsList(workspaceRef(), response.getCredentials());
    }
}
