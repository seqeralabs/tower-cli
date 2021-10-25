package io.seqera.tower.cli.commands;

import picocli.CommandLine;

@CommandLine.Command
public interface WithRequiredSubCommands extends Runnable {
    
    @CommandLine.Spec
    CommandLine.Model.CommandSpec getSpec();

    @Override
    default void run() {
        throw new CommandLine.ParameterException(getSpec().commandLine(), "Missing required subcommand");
    }
}
