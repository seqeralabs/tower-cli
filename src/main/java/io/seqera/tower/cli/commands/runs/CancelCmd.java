package io.seqera.tower.cli.commands.runs;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunCanceled;
import picocli.CommandLine;

@CommandLine.Command(
        name = "cancel",
        description = "Cancel a pipeline's execution"
)
public class CancelCmd extends AbstractRunsCmd {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline's run identifier", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException, IOException {
        try {
            api().cancelWorkflow(id, workspaceId(), null);

            return new RunCanceled(id, workspaceRef());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new RunNotFoundException(id, workspaceRef());
            }
            throw e;
        }
    }
}
