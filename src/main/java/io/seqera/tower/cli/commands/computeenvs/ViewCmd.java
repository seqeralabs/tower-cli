package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.responses.ComputeEnvView;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.DescribeComputeEnvResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "view",
        description = "View compute environment"
)
public class ViewCmd extends AbstractComputeEnvCmd {

    @Option(names = {"-i", "--id"}, description = "Compute environment identifier", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException {
        try {
            DescribeComputeEnvResponse response = api().describeComputeEnv(id, workspaceId());
            return new ComputeEnvView(id, workspaceRef(), response.getComputeEnv());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ComputeEnvNotFoundException(id, workspaceRef());
            }
            throw e;
        }
    }


}