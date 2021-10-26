package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.actions.create.CreateGitHubCmd;
import io.seqera.tower.cli.commands.actions.create.CreateTowerCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "create",
        description = "Create a new Pipeline Action",
        subcommands = {
                CreateGitHubCmd.class,
                CreateTowerCmd.class
        }
)
public class CreateCmd extends AbstractActionsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
