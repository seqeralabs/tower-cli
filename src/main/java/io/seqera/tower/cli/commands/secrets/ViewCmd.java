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
import io.seqera.tower.cli.responses.secrets.SecretView;
import io.seqera.tower.model.PipelineSecret;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "view",
        description = "View secret details."
)
public class ViewCmd extends AbstractSecretsCmd {

    @Mixin
    public WorkspaceOptionalOptions workspace;
    @Mixin
    SecretRefOptions ref;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineSecret secret = fetchSecret(ref, wspId);
        return new SecretView(workspaceRef(wspId), secret);
    }
}
