package io.seqera.tower.cli.commands.pipelines.versions;

import io.seqera.tower.cli.commands.AbstractRootCmd;
import picocli.CommandLine;


@CommandLine.Command(
        name = "versions",
        description = "Manage pipeline versions",
        subcommands = {
                ListCmd.class,
        }
)
public class VersionsCmd extends AbstractRootCmd {
}
