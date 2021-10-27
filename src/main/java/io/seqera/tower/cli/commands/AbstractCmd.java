package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.utils.VersionProvider;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        headerHeading = "%n",
        versionProvider = VersionProvider.class,
        mixinStandardHelpOptions = true,
        sortOptions = false,
        abbreviateSynopsis = true,
        descriptionHeading = "%n",
        commandListHeading = "%nCommands:%n",
        requiredOptionMarker = '*',
        usageHelpWidth = 160,
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n"
)
public abstract class AbstractCmd implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public CommandLine.Model.CommandSpec getSpec() {
        return spec;
    }
}
