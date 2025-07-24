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
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.labels.LabelUpdated;
import io.seqera.tower.cli.utils.ResponseHelper;
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
            UpdateLabelResponse res = labelsApi().updateLabel(labelId, req, wsp);
            return new LabelUpdated(res.getId(), res.getName(), res.getValue(), workspaceRef.workspace);
        } catch (ApiException e) {
            throw new TowerException(
                    String.format("Unable to update label '%d' for workspace '%d': %s", labelId, wsp, ResponseHelper.decodeMessage(e))
            );
        } catch (Exception e) {
            throw new TowerException(
                    String.format("Unable to update label '%d' for workspace '%d': %s", labelId, wsp, e.getMessage())
            );
        }
    }
}
