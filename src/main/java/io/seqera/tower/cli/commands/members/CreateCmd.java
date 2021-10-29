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

package io.seqera.tower.cli.commands.members;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.members.MembersCreated;
import io.seqera.tower.model.AddMemberRequest;
import io.seqera.tower.model.AddMemberResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "create",
        description = "Add a new organization member"
)
public class CreateCmd extends AbstractMembersClass {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User email to add as organization's member", required = true)
    public String user;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        AddMemberRequest request = new AddMemberRequest();
        request.setUser(user);

        AddMemberResponse response = api().createOrganizationMember(orgAndWorkspaceDbDto.getOrgId(), request);

        return new MembersCreated(organizationName, response.getMember());
    }
}
