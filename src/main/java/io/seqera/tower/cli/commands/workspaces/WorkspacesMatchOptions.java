package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine;

public class WorkspacesMatchOptions {

    static class MatchByName {
        @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
        public String workspaceName;

        @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
        public String organizationName;
    }


    static class MatchById {
        @CommandLine.Option(names = {"-i", "--id"}, description = "Workspace id", required = true)
        public Long workspaceId;
    }
}
