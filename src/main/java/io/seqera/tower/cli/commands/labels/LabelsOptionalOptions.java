package io.seqera.tower.cli.commands.labels;

import java.util.List;

import picocli.CommandLine;

public class LabelsOptionalOptions {

    @CommandLine.Option(names = {"--labels","-l"}, split = ",", description = "Labels to add", converter = Label.LabelConverter.class)
    public List<Label> labels = null;

}
