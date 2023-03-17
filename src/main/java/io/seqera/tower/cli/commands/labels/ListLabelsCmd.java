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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.AbstractComputeEnvCmd;
import io.seqera.tower.cli.commands.computeenvs.primary.GetCmd;
import io.seqera.tower.cli.commands.computeenvs.primary.SetCmd;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.LabelType;
import io.seqera.tower.model.ListLabelsResponse;
import picocli.CommandLine;

import javax.annotation.Nullable;
import java.io.IOException;

@CommandLine.Command(
        name = "list",
        description = "List labels"
)
public class ListLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspaceOptionalOptions;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Label type (normal|resource|all).", defaultValue = "all")
    public String labelType;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only labels that contain the given word.")
    @Nullable
    public String filter;

    @CommandLine.Mixin
    public PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspaceOptionalOptions.workspace);

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        LabelType typeEnum = LabelType.ALL;
        if (labelType == "simple") {
            typeEnum = LabelType.SIMPLE;
        } else if (labelType == "resource") {
            typeEnum = LabelType.RESOURCE;
        } else if (labelType == "all") {
            typeEnum = LabelType.ALL;
        } else {
            throw new IllegalArgumentException("Label type " + labelType + " is not supported");
        }

        ListLabelsResponse res = api().listLabels(wspId, max, offset, filter == null ? "" : filter, typeEnum);

        return new ListLabelsCmdResponse(wspId, res.getLabels());
    }
}
