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

    protected void deleteSecretByName(String name, Long wspId) throws SecretNotFoundException, ApiException {
        PipelineSecret secret = secretByName(wspId, name);
        deleteSecretById(secret.getId(), wspId);
    }

    protected void deleteSecretById(Long id, Long wspId) throws SecretNotFoundException, ApiException {
        api().deletePipelineSecret(id, wspId);
    }

}


