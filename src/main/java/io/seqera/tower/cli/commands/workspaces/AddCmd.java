/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceAdded;
import io.seqera.tower.model.CreateWorkspaceRequest;
import io.seqera.tower.model.CreateWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.Visibility;
import io.seqera.tower.model.Workspace;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add a new organization workspace."
)
public class AddCmd extends AbstractWorkspaceCmd {

    @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "The workspace organization name.", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-n", "--name"}, description = "The workspace short name. Only alphanumeric, dash and underscore characters are allowed.", required = true)
    public String workspaceName;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "The workspace full name.", required = true)
    public String workspaceFullName;

    @CommandLine.Option(names = {"-d", "--description"}, description = "The workspace description.")
    public String description;

    @CommandLine.Option(names = {"-v", "--visibility"}, description = "The workspace visibility. Valid options PRIVATE, SHARED [default: PRIVATE].")
    public String visibility = "PRIVATE";

    @Override
    protected Response exec() throws ApiException, IOException {

        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(organizationName);

        Workspace workspace = new Workspace();
        workspace.setName(workspaceName);
        workspace.setFullName(workspaceFullName);
        workspace.setDescription(description);
        workspace.setVisibility(parseVisibility());

        CreateWorkspaceRequest request = new CreateWorkspaceRequest().workspace(workspace);

        api().workspaceValidate(orgAndWorkspaceDbDto.getOrgId(), workspaceName);
        CreateWorkspaceResponse response = api().createWorkspace(orgAndWorkspaceDbDto.getOrgId(), request);

        return new WorkspaceAdded(response.getWorkspace().getName(), organizationName, response.getWorkspace().getVisibility());
    }

    private Visibility parseVisibility() throws ApiException {
        if ("PRIVATE".equals(visibility)) {
            return Visibility.PRIVATE;
        } else if ("SHARED".equals(visibility)) {
            return Visibility.SHARED;
        } else {
            throw new ApiException(
                    String.format("Invalid value for option '--visibility': expected one of [PRIVATE, SHARED] (case-sensitive) but was '%s'", visibility)
            );
        }
    }
}
