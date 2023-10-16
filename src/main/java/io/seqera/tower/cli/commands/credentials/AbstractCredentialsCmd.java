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

package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.ListCredentialsResponse;
import picocli.CommandLine.Command;

import java.util.Objects;

@Command
public abstract class AbstractCredentialsCmd extends AbstractApiCmd {

    protected Credentials findCredentialsByName(Long workspaceId, String name) throws ApiException {
        ListCredentialsResponse listCredentialsResponse = api().listCredentials(workspaceId, null);

        if (listCredentialsResponse == null || listCredentialsResponse.getCredentials() == null) {
            throw new CredentialsNotFoundException(name, workspaceId);
        }

        Credentials credentials = listCredentialsResponse.getCredentials().stream()
                .filter(it -> Objects.equals(it.getName(), name))
                .findFirst()
                .orElse(null);

        if (credentials == null) {
            throw new CredentialsNotFoundException(name, workspaceId);
        }

        return credentials;
    }

    protected Credentials fetchCredentials(CredentialsRefOptions credentialsRefOptions, Long wspId) throws ApiException {
        Credentials credentials;

        if (credentialsRefOptions.credentialsRef.credentialsId != null) {
            credentials = api().describeCredentials(credentialsRefOptions.credentialsRef.credentialsId, wspId).getCredentials();
        } else {
            credentials = findCredentialsByName(wspId, credentialsRefOptions.credentialsRef.credentialsName);
        }

        return credentials;
    }

    protected void deleteCredentialsByName(String name, Long wspId) throws CredentialsNotFoundException, ApiException {
        Credentials credentials = findCredentialsByName(wspId, name);
        deleteCredentialsById(credentials.getId(), wspId);
    }

    protected void deleteCredentialsById(String id, Long wspId) throws CredentialsNotFoundException, ApiException {
        api().deleteCredentials(id, wspId);
    }

}


