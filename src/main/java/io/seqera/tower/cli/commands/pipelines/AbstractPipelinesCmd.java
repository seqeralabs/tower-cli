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
import io.seqera.tower.model.PipelineQueryAttribute;
import picocli.CommandLine.Command;

import java.util.Collections;
import java.util.List;

@Command
public abstract class AbstractPipelinesCmd extends AbstractApiCmd {

    public AbstractPipelinesCmd() {
    }

    protected PipelineDbDto pipelineByName(Long workspaceId, String name) throws ApiException {

        ListPipelinesResponse list = api().listPipelines(Collections.emptyList(), workspaceId, null, null, name, "all");

        if (list.getPipelines().isEmpty()) {
            throw new PipelineNotFoundException(name, workspaceRef(workspaceId));
        }

        if (list.getPipelines().size() > 1) {
            throw new MultiplePipelinesFoundException(name, workspaceRef(workspaceId));
        }

        return list.getPipelines().get(0);
    }

    protected PipelineDbDto fetchPipeline(PipelineRefOptions pipelineRefOptions, Long wspId, PipelineQueryAttribute... attributes) throws ApiException {
        Long pipelineId = pipelineRefOptions.pipeline.pipelineId;
        if (pipelineId == null) {
            pipelineId = pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName).getPipelineId();
        }
        return api().describePipeline(pipelineId, List.of(attributes), wspId, null).getPipeline();
    }

}


