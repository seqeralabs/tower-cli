package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine;

public class WorkspacesMatchOptions {
    @CommandLine.ArgGroup( exclusive = false, heading = "Match by workspace and organization name")
    MatchByName matchByName;

    static class MatchByName {
        @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
        public String workspaceName;

        @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
        public String organizationName;
    }

    @CommandLine.ArgGroup(heading = "Match by workspace ID")
    MatchById matchById;

    static class MatchById {
        @CommandLine.Option(names = {"-i", "--id"}, description = "Workspace id", required = true)
        public Long workspaceId;
    }
}
