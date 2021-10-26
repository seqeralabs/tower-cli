package io.seqera.tower.cli.exceptions;

import picocli.CommandLine;

public class ShowUsageException extends TowerException {

    private CommandLine.Model.CommandSpec specs;

    public ShowUsageException(CommandLine.Model.CommandSpec specs){
        this.specs = specs;
    }

    public ShowUsageException(CommandLine.Model.CommandSpec specs,String message){
        super(message);
        this.specs = specs;
    }

    public CommandLine.Model.CommandSpec getSpecs() {
        return specs;
    }
}
