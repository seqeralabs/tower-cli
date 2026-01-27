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
import io.seqera.tower.cli.responses.CredentialsDeleted;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Credentials;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "delete",
        description = "Delete workspace credentials."
)
public class DeleteCmd extends AbstractCredentialsCmd {

    @CommandLine.Mixin
    CredentialsRefOptions credentialsRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String id;

        if (credentialsRefOptions.credentialsRef.credentialsId != null) {
            id = credentialsRefOptions.credentialsRef.credentialsId;
        } else {
            Credentials credentials = findCredentialsByName(wspId, credentialsRefOptions.credentialsRef.credentialsName);
            id = credentials.getId();
        }

        deleteCredentialsById(id, wspId);
        return new CredentialsDeleted(id, workspaceRef(wspId));
    }
}
