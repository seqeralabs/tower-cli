/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.labels.ListLabelsCmdResponse;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.LabelType;
import io.seqera.tower.model.ListLabelsResponse;
import picocli.CommandLine;

import jakarta.annotation.Nullable;
import java.io.IOException;

@CommandLine.Command(
        name = "list",
        description = "List labels"
)
public class ListLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspaceOptionalOptions;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Label type: normal, resource, or all (default: all)", defaultValue = "all")
    public LabelType labelType;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filter labels by substring")
    @Nullable
    public String filter;

    @CommandLine.Mixin
    public PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspaceOptionalOptions.workspace);

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);


        ListLabelsResponse res = labelsApi().listLabels(wspId, max, offset, filter == null ? "" : filter, labelType, null);

        return new ListLabelsCmdResponse(wspId, labelType, res.getLabels(), PaginationInfo.from(paginationOptions, res.getTotalSize()));
    }
}
