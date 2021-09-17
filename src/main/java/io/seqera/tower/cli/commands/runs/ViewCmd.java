package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunView;
import io.seqera.tower.model.DescribeWorkflowResponse;
import picocli.CommandLine;

@CommandLine.Command(
        name = "view",
        description = "View pipeline's runs"
)
public class ViewCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline's run identifier", required = true)
    public String id;

    protected Response exec() throws ApiException {
        try {
            DescribeWorkflowResponse response = api().describeWorkflow(id, workspaceId());

            return new RunView(id, workspaceRef(), response.getWorkflow());

        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new RunNotFoundException(id, workspaceRef());
            }

            throw e;
        }
    }
}
