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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.ContainerRegistryKeys;
import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class ContainerRegistryProvider extends AbstractProvider<ContainerRegistryKeys> {

    @Option(names = {"-u", "--username"}, description = "The user name to grant you access to the container registry.", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "The password to grant you access to the container registry.", arity = "0..1", interactive = true, required = true)
    public String password;

    @Option(names = {"-r", "--registry"}, description = "The container registry server name [default: 'docker.io'].", defaultValue = "docker.io")
    public String registry;

    public ContainerRegistryProvider() {
        super(ProviderEnum.CONTAINER_REG);
    }

    @Override
    public ContainerRegistryKeys securityKeys() throws IOException {
        return new ContainerRegistryKeys()
                .userName(userName)
                .password(password)
                .registry(registry);
    }
}