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

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.ShowLabelsOption;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsView;
import io.seqera.tower.model.DescribeActionResponse;
import io.seqera.tower.model.ListLabelsResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "view",
        description = "Describe an existing Pipeline Action."
)
public class ViewCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    ActionRefOptions actionRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public ShowLabelsOption showLabelsOption;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        DescribeActionResponse response = fetchDescribeActionResponse(actionRefOptions, wspId);

        String labels = showLabelsOption.showLabels ? commaSeparated(fetchLabelsFor(wspId)) : null;

        return new ActionsView(response.getAction(), baseWorkspaceUrl(wspId), labels);
    }

    private String commaSeparated(final ListLabelsResponse res) {
        return res.getLabels().stream().map(label -> {
            String str = label.getName();
            if (label.getValue() != null && !label.getValue().isEmpty()) {
                str += "=" + label.getValue();
            }
            return str;
        })
        .collect(Collectors.joining(","));
    }
}
