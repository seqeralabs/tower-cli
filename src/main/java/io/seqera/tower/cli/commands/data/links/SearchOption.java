package io.seqera.tower.cli.commands.data.links;

import picocli.CommandLine;

public class SearchOption {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Show only data links with names that start with the given word.")
    public String startsWith;

    @CommandLine.Option(names = {"-r", "--region"}, description = "Show only data links belonging to given region")
    public String region;

    @CommandLine.Option(names = {"-p", "--providers"}, description = "Show only data links belonging to given providers. [aws, azure, google]")
    public String providers;

    @CommandLine.Option(names = {"-u", "--uri"}, description = "Show only data links with URI (resource reference) that start with the given URI.")
    public String uri;

}
