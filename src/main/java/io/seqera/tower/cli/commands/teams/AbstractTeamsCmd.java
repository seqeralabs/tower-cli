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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Command
public abstract class AbstractTeamsCmd extends AbstractApiCmd {

    public AbstractTeamsCmd() {
    }

    public OrgAndWorkspaceDbDto findOrganizationByName(String organizationName) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new OrganizationNotFoundException(organizationName);
        }

        List<OrgAndWorkspaceDbDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceName(), null) && Objects.equals(item.getOrgName(), organizationName)
                )
                .collect(Collectors.toList());

        if (orgAndWorkspaceDbDtoList.isEmpty()) {
            throw new OrganizationNotFoundException(organizationName);
        }

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElse(null);
    }

    public MemberDbDto findMemberByUsername(Long orgId, Long teamId, String username) throws ApiException {
        ListMembersResponse listMembersResponse = api().listOrganizationTeamMembers(orgId, teamId);

        if (listMembersResponse.getMembers() == null) {
            throw new MemberNotFoundException(orgId, username);
        }

        return listMembersResponse
                .getMembers()
                .stream()
                .filter(item -> Objects.equals(item.getUserName(), username))
                .findFirst()
                .orElseThrow(()->new MemberNotFoundException(orgId, username));
    }

    public TeamDbDto findTeamByName(Long orgId, String teamName) throws ApiException {
        ListTeamResponse listTeamResponse = api().listOrganizationTeams(orgId, null, null);

        if (listTeamResponse == null) {
            throw new TeamNotFoundException(orgId, teamName);
        }

        return listTeamResponse
                .getTeams()
                .stream()
                .filter(item -> Objects.equals(item.getName(), teamName))
                .findFirst()
                .orElseThrow(() -> new TeamNotFoundException(orgId, teamName));
    }
}
