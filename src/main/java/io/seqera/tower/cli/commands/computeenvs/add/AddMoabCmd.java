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

import io.seqera.tower.cli.commands.computeenvs.platforms.MoabPlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "moab",
        description = "Add new MOAB compute environment."
)
public class AddMoabCmd extends AbstractAddCmd {

    @Mixin
    public MoabPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
