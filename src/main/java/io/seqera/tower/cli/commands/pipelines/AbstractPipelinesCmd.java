package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MultiplePipelinesFoundException;
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractPipelinesCmd extends AbstractApiCmd {

    public AbstractPipelinesCmd() {
    }

    protected PipelineDbDto pipelineByName(String name) throws ApiException {

        ListPipelinesResponse list = api().listPipelines(workspaceId(), null, null, name);

        if (list.getPipelines().isEmpty()) {
            throw new PipelineNotFoundException(name, workspaceRef());
        }

        if (list.getPipelines().size() > 1) {
            throw new MultiplePipelinesFoundException(name, workspaceRef());
        }

        return list.getPipelines().get(0);
    }

}


