/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

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

    protected PipelineDbDto pipelineByName(Long workspaceId, String name) throws ApiException {

        ListPipelinesResponse list = api().listPipelines(workspaceId, null, null, name);

        if (list.getPipelines().isEmpty()) {
            throw new PipelineNotFoundException(name, workspaceRef(workspaceId));
        }

        if (list.getPipelines().size() > 1) {
            throw new MultiplePipelinesFoundException(name, workspaceRef(workspaceId));
        }

        return list.getPipelines().get(0);
    }

    protected PipelineDbDto fetchPipeline(PipelineRefOptions pipelineRefOptions, Long wspId) throws ApiException {
        PipelineDbDto pipeline;

        if (pipelineRefOptions.pipeline.pipelineId != null) {
            pipeline = api().describePipeline(pipelineRefOptions.pipeline.pipelineId, wspId).getPipeline();
        } else {
            pipeline = pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName);
        }

        return pipeline;
    }

}


