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

package io.seqera.tower.cli.commands.computeenvs.add;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvAdded;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreateComputeEnvRequest;
import io.seqera.tower.model.Credentials;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Command
public abstract class AbstractAddCmd extends AbstractApiCmd {

    @Option(names = {"-n", "--name"}, description = "Compute environment name.", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-c", "--credentials"}, description = "Credentials identifier [default: workspace credentials].")
    public String credentialsRef;

    @Override
    protected Response exec() throws ApiException, IOException {
        return addComputeEnv(getPlatform().type(), getPlatform().computeConfig());
    }

    protected ComputeEnvAdded addComputeEnv(ComputeEnv.PlatformEnum platform, ComputeConfig config) throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        String credsId = credentialsRef == null ? findWorkspaceCredentials(platform, wspId) : credentialsByRef(platform, wspId, credentialsRef);

        api().createComputeEnv(
                new CreateComputeEnvRequest().computeEnv(
                        new ComputeEnv()
                                .name(name)
                                .platform(platform)
                                .credentialsId(credsId)
                                .config(config)
                ), wspId
        );

        return new ComputeEnvAdded(platform.getValue(), name, workspaceRef(wspId));
    }

    private String credentialsByRef(ComputeEnv.PlatformEnum type, Long wspId, String credentialsRef) throws ApiException {
        List<Credentials> credentials = api().listCredentials(wspId, type.getValue()).getCredentials();

        if (credentials.isEmpty()) {
            throw new TowerException("No valid credentials found at the workspace");
        }

        Credentials cred;

        cred = credentials.stream()
                .filter(it -> Objects.equals(it.getId(), credentialsRef) || Objects.equals(it.getName(), credentialsRef))
                .findFirst()
                .orElse(null);

        if (cred == null) {
            throw new TowerException("No valid credentials found at the workspace");
        }

        return cred.getId();
    }

    private String findWorkspaceCredentials(ComputeEnv.PlatformEnum type, Long wspId) throws ApiException {
        List<Credentials> credentials = api().listCredentials(wspId, type.getValue()).getCredentials();
        if (credentials.isEmpty()) {
            throw new TowerException("No valid credentials found at the workspace");
        }

        if (credentials.size() > 1) {
            throw new TowerException("Multiple credentials match this compute environment. Please provide the credentials identifier that you want to use");
        }

        return credentials.get(0).getId();
    }

    protected abstract Platform getPlatform();

}
