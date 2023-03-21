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
import picocli.CommandLine;

import javax.annotation.Nullable;
import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete label"
)
public class DeleteLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Label ID", required = true)
    public Long labelId;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspaceOptionalOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        try {

            Long wspId = workspaceId(workspaceOptionalOptions.workspace);

            api().deleteLabel(labelId, wspId);

        } catch (Exception e) {
            throw new TowerException(String.format("Unable to delete label '%d', reason: %s", labelId, e.toString()));
        }

        return new DeleteLabelsResponse(labelId, workspaceId(workspaceOptionalOptions.workspace));
    }
}
