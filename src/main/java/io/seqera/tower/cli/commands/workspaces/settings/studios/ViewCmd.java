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

package io.seqera.tower.cli.commands.workspaces.settings.studios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.workspaces.AbstractWorkspaceCmd;
import io.seqera.tower.cli.commands.workspaces.WorkspaceRefOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.StudiosSettingsView;
import io.seqera.tower.model.DataStudioWorkspaceSettingsResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.UpdateWorkspaceRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "view",
        description = "View the Studios settings of a workspace."
)
public class ViewCmd extends AbstractWorkspaceCmd {

    @CommandLine.Mixin
    public WorkspaceRefOptions workspaceRefOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDto ws = fetchOrgAndWorkspaceDbDto(workspaceRefOptions);

        DataStudioWorkspaceSettingsResponse settings = workspacesApi()
                .findDataStudiosWorkspaceSettings(ws.getOrgId(), ws.getWorkspaceId(), new UpdateWorkspaceRequest());

        return new StudiosSettingsView(ws.getWorkspaceName(), settings);
    }
}
