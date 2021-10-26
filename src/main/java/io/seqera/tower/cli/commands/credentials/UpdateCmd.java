package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.credentials.update.UpdateAwsCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateAzureCmd;
import io.seqera.tower.cli.commands.credentials.update.UpdateBitbucketCmd;
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
                UpdateGoogleCmd.class,
                UpdateGithubCmd.class,
                UpdateGitlabCmd.class,
                UpdateBitbucketCmd.class,
                UpdateSshCmd.class,
                UpdateK8sCmd.class,
                UpdateAzureCmd.class
        }
)
public class UpdateCmd extends AbstractCredentialsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
