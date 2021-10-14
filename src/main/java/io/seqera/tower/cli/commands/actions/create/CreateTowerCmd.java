package io.seqera.tower.cli.commands.actions.create;

import io.seqera.tower.model.ActionSource;
import picocli.CommandLine;

@CommandLine.Command(
        name = "tower",
        description = "Creates a Tower action"
)
public class CreateTowerCmd extends AbstractCreateCmd {
    @Override
    protected ActionSource getSource() {
        return ActionSource.TOWER;
    }
}
