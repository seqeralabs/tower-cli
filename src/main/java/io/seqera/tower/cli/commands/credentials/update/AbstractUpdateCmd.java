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

package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.credentials.AbstractCredentialsCmd;
import io.seqera.tower.cli.commands.credentials.CredentialsRefOptions;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.cli.responses.CredentialsUpdated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.UpdateCredentialsRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command
public abstract class AbstractUpdateCmd extends AbstractCredentialsCmd {

    @CommandLine.Mixin
    CredentialsRefOptions credentialsRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    public AbstractUpdateCmd() {
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        // Check that exists
        try {
//            DescribeCredentialsResponse response = api().describeCredentials(credentials, wspId);
            Credentials credentials = fetchCredentials(credentialsRefOptions, wspId);
            return update(credentials, wspId);
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                String ref = credentialsRefOptions.credentialsRef.credentialsId != null ? credentialsRefOptions.credentialsRef.credentialsId : credentialsRefOptions.credentialsRef.credentialsName;

                // Customize the forbidden message
                throw new CredentialsNotFoundException(ref, workspaceRef(wspId));
            }
            throw e;
        }
    }

    protected Response update(Credentials creds, Long wspId) throws ApiException, IOException {

        //TODO do we want to allow to change the name? name must be unique at workspace level?
        String name = creds.getName();

        Credentials specs = new Credentials();
        specs
                .keys(getProvider().securityKeys())
                .name(name)
                .baseUrl(getProvider().baseUrl())
                .provider(getProvider().type())
                .id(creds.getId());

        credentialsApi().updateCredentials(creds.getId(), new UpdateCredentialsRequest().credentials(specs), wspId);

        return new CredentialsUpdated(getProvider().type().name(), name, workspaceRef(wspId));
    }

    protected abstract CredentialsProvider getProvider();

}


