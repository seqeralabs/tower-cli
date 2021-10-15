package io.seqera.tower.cli.commands.participants;

import picocli.CommandLine;

public class ParticipantsOptions {

    @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "The organization name", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-w", "--wsp", "--workspace"}, description = "The workspace organization name", required = true)
    public String workspaceName;
}
