/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
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
        description = "Update a workspace secret."
)
public class UpdateCmd extends AbstractSecretsCmd {

    @Mixin
    public WorkspaceOptionalOptions workspace;
    @Option(names = {"-v", "--value"}, description = "Secret value.")
    public String value;
    @Mixin
    SecretRefOptions ref;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineSecret secret = fetchSecret(ref, wspId);
        api().updatePipelineSecret(secret.getId(), new UpdatePipelineSecretRequest().value(value), wspId);
        return new SecretUpdated(workspaceRef(wspId), secret.getName());
    }
}
