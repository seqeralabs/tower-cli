package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.RunsCmd;
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.Launch;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command
abstract public class AbstractRunsCmd extends AbstractApiCmd {

    @ParentCommand
    protected RunsCmd parent;

    public AbstractRunsCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
    }

    protected Launch launchByPipelineId(Long id) throws ApiException {
        DescribeLaunchResponse pipelineResponse = api().describePipelineLaunch(id, workspaceId());

        if (pipelineResponse == null) {
            throw new PipelineNotFoundException(id, workspaceRef());
        }

        return pipelineResponse.getLaunch();
    }
}
