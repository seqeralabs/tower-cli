package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.cli.commands.computeenv.create.CreateAltairCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateAwsCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateAwsManualCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateAzureCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateEksCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateGkeCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateGoogleCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateJsonCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateK8sCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateLsfCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateSlurmCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateUgeCmd;
import picocli.CommandLine.Command;

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
                CreateAwsManualCmd.class
        }
)
public class CreateCmd extends AbstractComputeEnvCmd {
}
