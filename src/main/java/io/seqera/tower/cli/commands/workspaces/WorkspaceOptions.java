package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine.Option;

public class WorkspaceOptions {

    @Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
    public String organizationName;

    @Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
    public String workspaceName;

    @Option(names = {"-f", "--fullName"}, description = "Workspace full name", required = true)
    public String workspaceFullName;

    @Option(names = {"-d", "--description"}, description = "Workspace description")
    public String description;


}
