/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.commands.secrets;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.SecretNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.secrets.SecretAdded;
import io.seqera.tower.model.CreatePipelineSecretRequest;
import io.seqera.tower.model.CreatePipelineSecretResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add a workspace secret."
)
public class AddCmd extends AbstractSecretsCmd {

    @Option(names = {"-n", "--name"}, description = "Secret name.", required = true)
    public String name;

    @Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-v", "--value"}, description = "Secret value.")
    public String value;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the secret if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        if (overwrite) tryDeleteSecret(name, wspId);
        CreatePipelineSecretResponse response = api().createPipelineSecret(new CreatePipelineSecretRequest().name(name).value(value), wspId);
        return new SecretAdded(workspaceRef(wspId), response.getSecretId(), name);
    }

    private void tryDeleteSecret(String name, Long wspId) throws ApiException {
        try {
            deleteSecretByName(name, wspId);
        } catch (SecretNotFoundException ignored) {}
    }
}
