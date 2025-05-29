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

package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.CredentialsList;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ListCredentialsResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List all workspace credentials."
)
public class ListCmd extends AbstractCredentialsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        
        ListCredentialsResponse response = credentialsApi().listCredentials(wspId, null);
        return new CredentialsList(workspaceRef(wspId), response.getCredentials(), baseWorkspaceUrl(wspId));
    }
}
