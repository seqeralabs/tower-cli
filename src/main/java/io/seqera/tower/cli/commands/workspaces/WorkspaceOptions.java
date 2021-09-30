package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine.Option;

public class WorkspaceOptions {

    @Option(names = {"-o", "--org", "--organization"}, description = "The workspace organization name", required = true)
    public String organizationName;

    @Option(names = {"-n", "--name"}, description = "The workspace short name. Only alphanumeric, dash and underscore characters are allowed", required = true)
    public String workspaceName;

    @Option(names = {"-d", "--description"}, description = "The workspace description")
    public String description;


}
