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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.ShowLabelsOption;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineQueryAttribute;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.List;

@Command(
        name = "list",
        description = "List workspace pipelines."
)
public class ListCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public ShowLabelsOption showLabelsOption;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only pipelines that contain the given word.")
    public String filter;

    @CommandLine.Option(names = {"--visibility"}, description = "Show pipelines: ${COMPLETION-CANDIDATES} [default: private].", defaultValue = "private")
    public PipelineVisibility visibility;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListPipelinesResponse response = new ListPipelinesResponse();

        List<PipelineQueryAttribute> pipelineQueryAttributes = showLabelsOption.showLabels ? List.of(PipelineQueryAttribute.LABELS) : NO_PIPELINE_ATTRIBUTES;

        try {

           response = api().listPipelines(pipelineQueryAttributes, wspId, max, offset, filter, visibility.toString());

        } catch (ApiException apiException) {
            if (apiException.getCode() == 404){
                throw new WorkspaceNotFoundException(wspId);
            }
        }

        return new PipelinesList(workspaceRef(wspId), response.getPipelines(), baseWorkspaceUrl(wspId), showLabelsOption.showLabels, PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
