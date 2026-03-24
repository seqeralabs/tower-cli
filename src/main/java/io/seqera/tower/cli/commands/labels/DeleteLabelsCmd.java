/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.labels.DeleteLabelsResponse;
import io.seqera.tower.cli.utils.ResponseHelper;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a label"
)
public class DeleteLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Label ID", required = true)
    public Long labelId;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspaceOptionalOptions;

    @Override
    protected Response exec() throws ApiException, TowerException, IOException {
        Long wspId = workspaceId(workspaceOptionalOptions.workspace);
        try {
            labelsApi().deleteLabel(labelId, wspId);
            return new DeleteLabelsResponse(labelId, workspaceId(workspaceOptionalOptions.workspace));
        } catch (ApiException e) {
            String reason = e.getResponseBody() == null && e.getCode() >= 400 && e.getCode() < 500
                    ? "Cannot find label with the provided ID"
                    : ResponseHelper.decodeMessage(e);
            throw new TowerException(String.format("Unable to delete label '%d' for workspace '%d': %s", labelId, wspId, reason));
        }
    }
}
