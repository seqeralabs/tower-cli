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
import io.seqera.tower.cli.responses.labels.LabelAdded;
import io.seqera.tower.cli.responses.labels.LabelUpdated;
import io.seqera.tower.model.UpdateLabelRequest;
import io.seqera.tower.model.UpdateLabelResponse;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update labels."
)
public class UpdateLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Label ID", required = true)
    public Long labelId;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1",heading = "Update: at least one of the following options must be provided.\n")
    public UpdateOptions updateOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspaceRef;

    static class UpdateOptions {
        @CommandLine.Option(names = {"-n", "--name"}, description = "Label name.")
        public String labelName;

        @CommandLine.Option(names = {"-v", "--value"}, description = "Label value.")
        public String labelValue;
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        UpdateLabelRequest req = new UpdateLabelRequest()
                .name(updateOptions.labelName)
                .value(updateOptions.labelValue);
        Long wsp = workspaceId(workspaceRef.workspace);
        try {
            UpdateLabelResponse res = api().updateLabel(labelId, req, wsp);
            return new LabelUpdated(res.getId(),res.getName(),res.getValue(),workspaceRef.workspace);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to update label '%d' for workspace '%d'",labelId, wsp));
        }
    }
}
