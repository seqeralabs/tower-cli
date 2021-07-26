package io.seqera.tower.cli.autocomplete;

import picocli.AutoComplete;
import picocli.CommandLine;

@CommandLine.Command(name = "generate-completion", description = "Generate autocomplete script")
public class AutocompleteCmd implements Runnable {

    private CommandLine cmd;

    public AutocompleteCmd(CommandLine cmd) {
        this.cmd = cmd;
    }

    @Override
    public void run() {
        System.out.println(AutoComplete.bash("towr", cmd));
    }
}
