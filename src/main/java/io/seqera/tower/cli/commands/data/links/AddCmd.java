package io.seqera.tower.cli.commands.data.links;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkView;
import io.seqera.tower.cli.utils.data.DataLinkProvider;
import io.seqera.tower.model.DataLinkDto;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;

@CommandLine.Command(
        name = "add",
        description = "Add custom data link."
)
public class AddCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Data link name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Data link description.")
    public String description;

    @CommandLine.Option(names = {"-u", "--uri"}, description = "Data link uri.", required = true)
    public String url;

    @CommandLine.Option(names = {"-p", "--provider"}, description = "Data link provider. [aws, azure, google]", required = true)
    public DataLinkProvider provider;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.")
    public String credentialsRef;


    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        DataLinkDto created = addDataLink(wspId, name, description, url, provider.name(), credentialsRef);

        if (Objects.isNull(created)) {
            app().getOut().println("Data link creation failed.");
            return null;
        }

        return new DataLinkView(created);
    }

}
