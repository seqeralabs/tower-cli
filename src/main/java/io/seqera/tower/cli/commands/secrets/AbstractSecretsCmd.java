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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.SecretNotFoundException;
import io.seqera.tower.model.ListPipelineSecretsResponse;
import io.seqera.tower.model.PipelineSecret;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractSecretsCmd extends AbstractApiCmd {

    public AbstractSecretsCmd() {
    }

    protected PipelineSecret secretByName(Long workspaceId, String name) throws ApiException {
        ListPipelineSecretsResponse list = api().listPipelineSecrets(workspaceId);
        for (PipelineSecret secret : list.getPipelineSecrets()) {
            if (name.equals(secret.getName())) {
                return secret;
            }
        }
        throw new SecretNotFoundException(name, workspaceRef(workspaceId));
    }

    protected PipelineSecret fetchSecret(SecretRefOptions ref, Long wspId) throws ApiException {
        return ref.secret.id != null ? api().describePipelineSecret(ref.secret.id, wspId).getPipelineSecret() : secretByName(wspId, ref.secret.name);
    }

}


