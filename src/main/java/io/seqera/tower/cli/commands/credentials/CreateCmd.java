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
import io.seqera.tower.cli.commands.credentials.create.CreateAwsCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateAzureCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateBitbucketCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateGithubCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateGitlabCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateGoogleCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateK8sCmd;
import io.seqera.tower.cli.commands.credentials.create.CreateSshCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "create",
        description = "Create new workspace credentials.",
        subcommands = {
                CreateAwsCmd.class,
                CreateGoogleCmd.class,
                CreateGithubCmd.class,
                CreateGitlabCmd.class,
                CreateBitbucketCmd.class,
                CreateSshCmd.class,
                CreateK8sCmd.class,
                CreateAzureCmd.class
        }
)
public class CreateCmd extends AbstractCredentialsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
