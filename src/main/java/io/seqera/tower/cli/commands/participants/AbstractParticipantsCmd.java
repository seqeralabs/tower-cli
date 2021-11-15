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

package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.ParticipantNotFoundException;
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.ListParticipantsResponse;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public class AbstractParticipantsCmd extends AbstractApiCmd {

    public AbstractParticipantsCmd() {
    }

    protected OrgAndWorkspaceDbDto findOrgAndWorkspaceByName(String organizationName, String workspaceName) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse == null || workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            if (workspaceName == null) {
                throw new OrganizationNotFoundException(organizationName);
            }

            throw new WorkspaceNotFoundException(workspaceName, organizationName);
        }

        List<OrgAndWorkspaceDbDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceName(), workspaceName) && Objects.equals(item.getOrgName(), organizationName)
                )
                .collect(Collectors.toList());

        if (orgAndWorkspaceDbDtoList.isEmpty()) {
            if (workspaceName == null) {
                throw new OrganizationNotFoundException(organizationName);
            }

            throw new WorkspaceNotFoundException(workspaceName, organizationName);
        }

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElse(null);
    }

    protected MemberDbDto findOrganizationMemberByName(Long orgId, String name) throws ApiException {
        ListMembersResponse listMembersResponse = api().listOrganizationMembers(orgId, null, null, name);

        MemberDbDto member;

        if (listMembersResponse.getMembers() != null) {
            member = listMembersResponse.getMembers().stream().findFirst().orElse(null);

            if (member != null) {
                return member;
            }
        }

        throw new MemberNotFoundException(orgId, name);
    }

    protected MemberDbDto findOrganizationCollaboratorByName(Long orgId, String name) throws ApiException {
        ListMembersResponse listCollaboratorsResponse = api().listOrganizationCollaborators(orgId, null, null, name);

        MemberDbDto member;

        if (listCollaboratorsResponse.getMembers() != null) {
            member = listCollaboratorsResponse.getMembers().stream().findFirst().orElse(null);

            if (member != null) {
                return member;
            }
        }

        throw new MemberNotFoundException(orgId, name);
    }

    protected TeamDbDto findOrganizationTeamByName(Long orgId, String name) throws ApiException {
        ListTeamResponse listTeamResponse = api().listOrganizationTeams(orgId, null, null);

        if (listTeamResponse.getTeams() != null) {
            TeamDbDto team = listTeamResponse.getTeams().stream().filter(it -> Objects.equals(it.getName(), name)).findFirst().orElse(null);

            if (team != null) {
                return team;
            }
        }

        throw new TeamNotFoundException(orgId, name);
    }

    protected ParticipantDbDto findWorkspaceParticipant(Long organizationId, Long workspaceId, String name) throws ApiException {
        ListParticipantsResponse listParticipantsResponse = api().listWorkspaceParticipants(organizationId, workspaceId, null, null, name);

        if (listParticipantsResponse.getParticipants() != null) {
            ParticipantDbDto participant = listParticipantsResponse.getParticipants().stream().findFirst().orElse(null);

            if (participant != null) {
                return participant;
            }
        }

        throw new ParticipantNotFoundException(workspaceId, name);
    }
}
