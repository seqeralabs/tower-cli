package io.seqera.tower.cli.commands.organizations;

import picocli.CommandLine;

public class OrganizationsOptions {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Organization name", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Organization description")
    public String description;

    @CommandLine.Option(names = {"-l", "--location"}, description = "Organization location")
    public String location;

    @CommandLine.Option(names = {"-w", "--website"}, description = "Organization website URL")
    public String website;
}
