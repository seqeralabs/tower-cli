package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine;

public class WorkspacesMatchOptions {
    @CommandLine.ArgGroup( exclusive = false, heading = "%nMatch by workspace and organization name:%n")
    MatchByName matchByName;

    static class MatchByName {
        @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
        public String workspaceName;

        @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
        public String organizationName;
    }

    @CommandLine.ArgGroup(heading = "%nMatch by workspace ID:%n")
    MatchById matchById;

    static class MatchById {
        @CommandLine.Option(names = {"-i", "--id"}, description = "Workspace id", required = true)
        public Long workspaceId;
    }
}
