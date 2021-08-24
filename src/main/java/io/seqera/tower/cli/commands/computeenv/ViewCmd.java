package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.ComputeEnvDeleted;
import io.seqera.tower.cli.responses.ComputeEnvView;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.DescribeComputeEnvResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "view",
        description = "View compute environment"
)
public class ViewCmd extends AbstractComputeEnvCmd {

    @CommandLine.Option(names = {"-i", "--id"}, required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException {
        try {
            DescribeComputeEnvResponse response = api().describeComputeEnv(id, workspaceId());
            return new ComputeEnvView(id, workspaceRef(), response.getComputeEnv());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ApiException(404, String.format("Unknown compute environment with id '%s' at %s workspace", id, workspaceRef()));
            }
            throw e;
        }
    }
}
