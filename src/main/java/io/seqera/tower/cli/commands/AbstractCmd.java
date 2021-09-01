package io.seqera.tower.cli.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        headerHeading = "%n",
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
}
