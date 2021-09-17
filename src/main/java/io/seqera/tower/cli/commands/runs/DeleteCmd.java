package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunDeleted;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a pipeline's execution"
)
public class DeleteCmd extends AbstractRunsCmd {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline's run identifier", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException, IOException {
        try {
            api().deleteWorkflow(id, workspaceId());

            return new RunDeleted(id, workspaceRef());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new RunNotFoundException(id, workspaceRef());
            }
            throw e;
        }
    }
}
