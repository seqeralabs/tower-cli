package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class WorkspacesMatchOptions {

    @ArgGroup(multiplicity = "1")
    public ByNameOrId match;

    public static class ByNameOrId {
        @ArgGroup(exclusive = false, heading = "%nMatch by workspace and organization name:%n")
        public MatchByName byName;

        @ArgGroup(heading = "%nMatch by workspace ID:%n")
        public MatchById byId;
    }


    public static class MatchByName {
        @Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
        public String workspaceName;

        @Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
        public String organizationName;
    }

    public static class MatchById {
        @Option(names = {"-i", "--id"}, description = "Workspace id", required = true)
        public Long workspaceId;
    }
}
