/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.data.links;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkView;
import io.seqera.tower.model.DataLinkDto;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;

@CommandLine.Command(
        name = "update",
        description = "Update custom data link."
)
public class UpdateCmd extends AbstractDataLinksCmd {
    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-i", "--id"}, description = "Data link id.", required = true)
    public String id;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Data link name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Data link description.")
    public String description;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.")
    public String credentialsRef;


    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        DataLinkDto updated = updateDataLink(wspId, id, name, description, credentialsRef);

        if (Objects.isNull(updated)) {
            app().getOut().println("Data link update failed.");
            return null;
        }

        return new DataLinkView(updated, "Data link updated");
    }

}
