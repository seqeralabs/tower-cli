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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.primary.GetCmd;
import io.seqera.tower.cli.commands.computeenvs.primary.SetCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "primary",
        description = "Sets or gets a primary compute environment within current workspace.",
        subcommands = {
                GetCmd.class,
                SetCmd.class,
        }
)
public class PrimaryCmd extends AbstractComputeEnvCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
