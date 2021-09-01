package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.ComputeEnvList;
import io.seqera.tower.cli.responses.CredentialsList;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ListComputeEnvsResponse;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        aliases = "l",
        description = "List all workspace compute environments"
)
public class ListCmd extends AbstractComputeEnvCmd {

    @Override
    protected Response exec() throws ApiException, IOException {
        ListComputeEnvsResponse response = api().listComputeEnvs(null, workspaceId());
        return new ComputeEnvList(response.getComputeEnvs());
    }
}
