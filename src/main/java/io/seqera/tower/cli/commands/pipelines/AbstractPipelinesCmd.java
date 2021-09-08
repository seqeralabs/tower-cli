package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.PipelinesCmd;
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command
public abstract class AbstractPipelinesCmd extends AbstractApiCmd {

    @ParentCommand
    protected PipelinesCmd parent;

    public AbstractPipelinesCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
    }

    protected PipelineDbDto pipelineByName(String name) throws ApiException {

        ListPipelinesResponse list = api().listPipelines(workspaceId(), null, null, name);

        if (list.getPipelines().isEmpty()) {
            throw new PipelineNotFoundException(name, workspaceRef());
        }

        if (list.getPipelines().size() > 1) {
            throw new TowerException(String.format("Multiple pipelines match '%s' at %s workspace", name, workspaceRef()));
        }

        return list.getPipelines().get(0);
    }

}


