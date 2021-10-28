package io.seqera.tower.cli.commands.computeenvs;


import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAltairCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAwsCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAzureCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateEksCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateGkeCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateGoogleCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateJsonCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateK8sCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateLsfCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateSlurmCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateUgeCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "create",
        description = "Create new compute environment",
        subcommands = {
                CreateK8sCmd.class,
                CreateAwsCmd.class,
                CreateJsonCmd.class,
                CreateEksCmd.class,
                CreateSlurmCmd.class,
                CreateLsfCmd.class,
                CreateUgeCmd.class,
                CreateAltairCmd.class,
                CreateGkeCmd.class,
                CreateGoogleCmd.class,
                CreateAzureCmd.class,
        }
)
public class CreateCmd extends AbstractComputeEnvCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
