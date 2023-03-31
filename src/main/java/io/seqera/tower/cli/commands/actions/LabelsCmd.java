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
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.labels.LabelsSubcmdOptions;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collections;

@CommandLine.Command(name = "labels", description = "Manages labels for actions.")
public class LabelsCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    public ActionRefOptions actionRefOptions;

    @CommandLine.Mixin
    public LabelsSubcmdOptions labelsSubcmdOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(labelsSubcmdOptions.workspace.workspace);
        DefaultApi api = api();
        String actionId = fetchDescribeActionResponse(actionRefOptions, wspId,Collections.emptyList()).getAction().getId();
        ActionsLabelsManager creator = new ActionsLabelsManager(api);
        return creator.execute(wspId, actionId, labelsSubcmdOptions);
    }
}
