package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
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
    protected Integer exec() throws ApiException, IOException {
        ListCredentialsResponse response = api().listCredentials(workspaceId(), null);
        for (Credentials cred : response.getCredentials()) {
            app().println(String.format("- [%s] (%s) %s%s", cred.getId(), cred.getProvider(), cred.getName(), formatDescription(cred.getDescription())));
        }
        return 0;
    }

    private String formatDescription(String value) {
        if (value == null) {
            return "";
        }
        return String.format(" - %s", value);
    }
}
