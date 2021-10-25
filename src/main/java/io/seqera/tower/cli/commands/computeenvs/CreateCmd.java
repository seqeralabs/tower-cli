package io.seqera.tower.cli.commands.computeenvs;


import io.seqera.tower.cli.commands.WithRequiredSubCommands;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAltairCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAwsCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAwsManualCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAzureCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAzureManualCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateEksCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateGkeCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateGoogleCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateJsonCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateK8sCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateLsfCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateSlurmCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateUgeCmd;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "create",
        description = "Create new compute environment",
        subcommands = {
                CreateK8sCmd.class,
                CreateAwsCmd.class,
                CreateAwsManualCmd.class,
                CreateJsonCmd.class,
                CreateEksCmd.class,
                CreateSlurmCmd.class,
                CreateLsfCmd.class,
                CreateUgeCmd.class,
                CreateAltairCmd.class,
                CreateGkeCmd.class,
                CreateGoogleCmd.class,
                CreateAzureCmd.class,
                CreateAzureManualCmd.class
        }
)
public class CreateCmd extends AbstractComputeEnvCmd implements WithRequiredSubCommands {


}
