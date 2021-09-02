package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.ComputeEnvDeleted;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "delete",
        description = "Delete compute environment"
)
public class DeleteCmd extends AbstractComputeEnvCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Compute environment identifier", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException {
        try {
            api().deleteComputeEnv(id, workspaceId());
            return new ComputeEnvDeleted(id, workspaceRef());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ApiException(404, String.format("Unknown compute environment with id '%s' at %s workspace", id, workspaceRef()));
            }
            throw e;
        }
    }
}
