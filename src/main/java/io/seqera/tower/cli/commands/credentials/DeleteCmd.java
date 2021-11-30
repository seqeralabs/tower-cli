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

package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
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

        try {
            api().deleteCredentials(id, wspId);
            return new CredentialsDeleted(id, workspaceRef(wspId));
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new CredentialsNotFoundException(id, workspaceRef(wspId));
            }
            throw e;
        }
    }
}
