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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.labels.DeleteLabelsResponse;
import picocli.CommandLine;

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
        Long wspId = workspaceId(workspaceOptionalOptions.workspace);
        labelsApi().deleteLabel(labelId, wspId);
        return new DeleteLabelsResponse(labelId, workspaceId(workspaceOptionalOptions.workspace));
    }
}
