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

package io.seqera.tower.cli.commands.credentials.add;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.credentials.AbstractCredentialsCmd;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.cli.responses.CredentialsAdded;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.CreateCredentialsRequest;
import io.seqera.tower.model.CreateCredentialsResponse;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command
public abstract class AbstractAddCmd<T extends SecurityKeys> extends AbstractCredentialsCmd {

    @Option(names = {"-n", "--name"}, description = "Credentials name.", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the credentials if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        Credentials specs = new Credentials();
        specs
            .keys(getProvider().securityKeys())
            .name(name)
            .baseUrl(getProvider().baseUrl())
            .provider(getProvider().type());

        if (overwrite) tryDeleteCredentials(name, wspId);

        CreateCredentialsResponse resp = api().createCredentials(new CreateCredentialsRequest().credentials(specs), wspId);

        return new CredentialsAdded(getProvider().type().name(), resp.getCredentialsId(), name, workspaceRef(wspId));
    }

    protected abstract CredentialsProvider getProvider();

    private void tryDeleteCredentials(String name, Long wspId) throws ApiException {
        try {
            deleteCredentialsByName(name, wspId);
        } catch (CredentialsNotFoundException ignored) {}
    }
}
