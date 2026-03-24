/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.credentials.add.AddAgentCmd;
import io.seqera.tower.cli.commands.credentials.add.AddAwsCmd;
import io.seqera.tower.cli.commands.credentials.add.AddAzureCmd;
import io.seqera.tower.cli.commands.credentials.add.AddBitbucketCmd;
import io.seqera.tower.cli.commands.credentials.add.AddCodeCommitCmd;
import io.seqera.tower.cli.commands.credentials.add.AddContainerRegistryCmd;
import io.seqera.tower.cli.commands.credentials.add.AddGiteaCmd;
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
        description = "Add workspace credentials",
        subcommands = {
                AddAwsCmd.class,
                AddCodeCommitCmd.class,
                AddGoogleCmd.class,
                AddGithubCmd.class,
                AddGitlabCmd.class,
                AddGiteaCmd.class,
                AddBitbucketCmd.class,
                AddSshCmd.class,
                AddK8sCmd.class,
                AddAzureCmd.class,
                AddAgentCmd.class,
                AddContainerRegistryCmd.class
        }
)
public class AddCmd extends AbstractCredentialsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
