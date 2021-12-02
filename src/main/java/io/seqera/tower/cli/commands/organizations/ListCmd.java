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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsList;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "list",
        description = "List available organizations."
)
public class ListCmd extends AbstractOrganizationsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {

        ListWorkspacesAndOrgResponse response = api().listWorkspacesUser(userId());


        if (response.getOrgsAndWorkspaces() != null) {
            List<OrgAndWorkspaceDbDto> responseOrg = response.getOrgsAndWorkspaces()
                    .stream()
                    .filter(item -> Objects.equals(item.getWorkspaceId(), null))
                    .collect(Collectors.toList());

            return new OrganizationsList(userName(), responseOrg, serverUrl());
        }

        return new OrganizationsList(userName(), response.getOrgsAndWorkspaces(), serverUrl());
    }
}
