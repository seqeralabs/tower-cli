package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.organizations.CreateCmd;
import io.seqera.tower.cli.commands.organizations.DeleteCmd;
import io.seqera.tower.cli.commands.organizations.ListCmd;
import io.seqera.tower.cli.commands.organizations.UpdateCmd;
import io.seqera.tower.cli.commands.organizations.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "organizations",
        description = "Manage organizations",
        subcommands = {
                ListCmd.class,
                DeleteCmd.class,
                CreateCmd.class,
                UpdateCmd.class,
                ViewCmd.class,
        }
)
public class OrganizationsCmd extends AbstractRootCmd {
}
