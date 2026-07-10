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

package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.CredentialsValidated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.ValidateCredentialsResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "validate",
        description = "Validate workspace credentials against their cloud provider"
)
public class ValidateCmd extends AbstractCredentialsCmd {

    @CommandLine.Mixin
    CredentialsRefOptions credentialsRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"--force"}, description = "Skip the provider probe and force an INVALID credential to AVAILABLE. Rejected if the credential is not INVALID.")
    public boolean force;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        Credentials credentials = fetchCredentials(credentialsRefOptions, wspId);

        ValidateCredentialsResponse response = credentialsApi().validateCredentials(credentials.getId(), wspId, force);

        return new CredentialsValidated(
                credentials.getId(),
                credentials.getName(),
                workspaceRef(wspId),
                response.getStatus(),
                Boolean.TRUE.equals(response.getTransientError()),
                response.getMessage()
        );
    }
}
