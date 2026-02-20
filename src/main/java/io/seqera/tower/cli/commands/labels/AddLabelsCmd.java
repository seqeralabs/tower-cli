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
import io.seqera.tower.cli.responses.labels.LabelAdded;
import io.seqera.tower.cli.utils.ResponseHelper;
import io.seqera.tower.model.CreateLabelRequest;
import io.seqera.tower.model.CreateLabelResponse;
import picocli.CommandLine;

import jakarta.annotation.Nullable;
import java.io.IOException;

@CommandLine.Command(
        name = "add",
        description = "Add a label"
)
public class AddLabelsCmd extends AbstractLabelsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Label name", required = true)
    public String labelName;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspaceOptionalOptions;

    @CommandLine.Option(names = {"-v", "--value"}, description = "Label value")
    @Nullable
    public String labelValue;

    @Override
    protected Response exec() throws ApiException, IOException, IllegalArgumentException, TowerException {

        CreateLabelRequest req = new CreateLabelRequest()
                .name(labelName)
                .value(labelValue)
                .resource(labelValue != null);

        if (labelValue != null) {
            req.setValue(labelValue);
            req.setResource(true);
        } else {
            req.setResource(false);
        }

        Long wspId = workspaceId(workspaceOptionalOptions.workspace);

        try {
            CreateLabelResponse res = labelsApi().createLabel(req, wspId);
            return new LabelAdded(res.getId(), res.getName(), res.getResource(), res.getValue(), workspaceOptionalOptions.workspace);
        } catch (ApiException apiException) {
            throw new TowerException(
                String.format("Unable to create label for workspace '%d': %s", wspId, ResponseHelper.decodeMessage(apiException))
            );
        } catch (Exception e) {
            throw new TowerException(
                    String.format("Unable to create label for workspace '%d': %s", wspId, e.getMessage())
            );
        }
    }

}
