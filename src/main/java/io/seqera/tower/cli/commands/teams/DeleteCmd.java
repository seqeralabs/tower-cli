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

package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete an organization team"
)
public class DeleteCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Team's identifier", required = true)
    public Long teamId;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        api().deleteOrganizationTeam(orgAndWorkspaceDbDto.getOrgId(), teamId);

        return new TeamDeleted(organizationName, teamId.toString());
    }
}
