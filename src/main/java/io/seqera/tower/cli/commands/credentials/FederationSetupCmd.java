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
import io.seqera.tower.cli.responses.CredentialsFederationSetup;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CredentialsSetupValuesResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "federation-setup",
        description = "Show the values to configure in your cloud provider before adding workload identity credentials"
)
public class FederationSetupCmd extends AbstractCredentialsCmd {

    // Platform returns no values, rather than an error, for any other provider
    public enum Provider { aws, google }

    @Option(names = {"-p", "--provider"}, description = "Credentials provider: ${COMPLETION-CANDIDATES}.", required = true)
    public Provider provider;

    @Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        CredentialsSetupValuesResponse response = credentialsApi().describeCredentialsSetup(provider.name(), wspId);
        return new CredentialsFederationSetup(provider.name(), workspaceRef(wspId), response.getSetupValues());
    }
}
