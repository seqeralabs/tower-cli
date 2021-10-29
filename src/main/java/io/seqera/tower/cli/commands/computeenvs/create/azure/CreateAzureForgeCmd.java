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

package io.seqera.tower.cli.commands.computeenvs.create.azure;

import io.seqera.tower.cli.commands.computeenvs.create.AbstractCreateCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.AzBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "forge",
        description = "Create new Azure Batch compute environment with automatic provisioning of compute resources"
)
public class CreateAzureForgeCmd extends AbstractCreateCmd {

    @Mixin
    public AzBatchForgePlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
