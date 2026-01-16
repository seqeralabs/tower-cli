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

package io.seqera.tower.cli.commands.secrets;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.secrets.SecretUpdated;
import io.seqera.tower.model.PipelineSecret;
import io.seqera.tower.model.UpdatePipelineSecretRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "update",
        description = "Update a secret"
)
public class UpdateCmd extends AbstractSecretsCmd {

    @Mixin
    public WorkspaceOptionalOptions workspace;
    @Option(names = {"-v", "--value"}, description = "New secret value, to be stored securely. The secret is made available to pipeline executions at runtime.")
    public String value;
    @Mixin
    SecretRefOptions ref;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineSecret secret = fetchSecret(ref, wspId);
        pipelineSecretsApi().updatePipelineSecret(secret.getId(), new UpdatePipelineSecretRequest().value(value), wspId);
        return new SecretUpdated(workspaceRef(wspId), secret.getName());
    }
}
