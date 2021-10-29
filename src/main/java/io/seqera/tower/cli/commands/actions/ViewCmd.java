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

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsView;
import io.seqera.tower.model.DescribeActionResponse;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

@CommandLine.Command(
        name = "view",
        description = "Describe an existing Pipeline Action"
)
public class ViewCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(actionName);

        DescribeActionResponse response = api().describeAction(listActionsResponseActionInfo.getId(), workspaceId());

        return new ActionsView(response.getAction());
    }
}
