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

import io.seqera.tower.cli.commands.credentials.providers.ContainerRegistryProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "container-reg",
        description = "Add new Container Registry workspace credentials."
)
public class AddContainerRegistryCmd extends AbstractAddCmd {

    @Mixin
    ContainerRegistryProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
