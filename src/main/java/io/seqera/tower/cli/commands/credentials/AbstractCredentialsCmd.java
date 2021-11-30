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

}


