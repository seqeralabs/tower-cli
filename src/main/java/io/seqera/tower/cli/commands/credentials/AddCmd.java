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
import io.seqera.tower.cli.commands.credentials.add.AddAgentCmd;
import io.seqera.tower.cli.commands.credentials.add.AddAwsCmd;
import io.seqera.tower.cli.commands.credentials.add.AddAzureCmd;
import io.seqera.tower.cli.commands.credentials.add.AddBitbucketCmd;
import io.seqera.tower.cli.commands.credentials.add.AddGithubCmd;
import io.seqera.tower.cli.commands.credentials.add.AddGitlabCmd;
import io.seqera.tower.cli.commands.credentials.add.AddGoogleCmd;
import io.seqera.tower.cli.commands.credentials.add.AddK8sCmd;
import io.seqera.tower.cli.commands.credentials.add.AddSshCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add new workspace credentials.",
        subcommands = {
                AddAwsCmd.class,
                AddGoogleCmd.class,
                AddGithubCmd.class,
                AddGitlabCmd.class,
                AddBitbucketCmd.class,
                AddSshCmd.class,
                AddK8sCmd.class,
                AddAzureCmd.class,
                AddAgentCmd.class,
        }
)
public class AddCmd extends AbstractCredentialsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
