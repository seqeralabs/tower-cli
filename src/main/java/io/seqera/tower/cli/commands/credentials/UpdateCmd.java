/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.credentials.update.UpdateAgentCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateAwsCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateAzureCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateBitbucketCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateCodeCommitCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateContainerRegistryCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateGithubCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateGitlabCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateGoogleCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateK8sCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateSshCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "update",
        description = "Update workspace credentials",
        subcommands = {
                UpdateAwsCmd.class,
                UpdateCodeCommitCmd.class,
                UpdateGoogleCmd.class,
                UpdateGithubCmd.class,
                UpdateGitlabCmd.class,
                UpdateBitbucketCmd.class,
                UpdateSshCmd.class,
                UpdateK8sCmd.class,
                UpdateAzureCmd.class,
                UpdateContainerRegistryCmd.class,
                UpdateAgentCmd.class
        }
)
public class UpdateCmd extends AbstractCredentialsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
