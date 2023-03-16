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
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CreateLabelRequest;
import io.seqera.tower.model.CreateLabelResponse;
import picocli.CommandLine;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;

@CommandLine.Command(
        name = "add",
        description = "Add new label"
)
public class AddLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Label name.", required = true)
    public String labelName;

    @CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace numeric identifier", required = false)
    @Nullable
    public Long workspaceId;

    @CommandLine.Option(names = {"-v", "--value"}, description = "label value", required = false)
    @Nullable
    public String labelValue;

    @Override
    protected Response exec() throws ApiException, IOException, IllegalArgumentException, TowerException {

        if (labelName == null || labelName.isEmpty()) {
            throw new IllegalArgumentException("Missing label name");
        }

        CreateLabelRequest req = new CreateLabelRequest();

        req.setName(labelName);

        if (labelValue != null) {
            req.setValue(labelValue);
            req.setResource(true);
        } else {
            req.setResource(false);
        }

        CreateLabelResponse res = null;
        try {
            res = api().createLabel(req, workspaceId);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to create label for workspace '%d'", workspaceId));
        }

        return new GenericStrResponse(String.format("successfully created label '%d' for workspace '%d'\n", res.getId(), workspaceId));
    }

}
