/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
import io.seqera.tower.cli.utils.Paginator;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine.Command;

import java.util.Objects;

@Command
public abstract class AbstractTeamsCmd extends AbstractApiCmd {

    public AbstractTeamsCmd() {
    }

    public MemberDbDto findMemberByUsername(Long orgId, Long teamId, String username) throws ApiException {
        return Paginator.findFirst(
                (max, offset) -> teamsApi().listOrganizationTeamMembers(orgId, teamId, max, offset, username),
                ListMembersResponse::getMembers,
                ListMembersResponse::getTotalSize,
                item -> Objects.equals(item.getUserName(), username)
        ).orElseThrow(() -> new MemberNotFoundException(orgId, username));
    }

    public TeamDbDto findTeamByName(Long orgId, String teamName) throws ApiException {
        // The search also matches other names and descriptions containing teamName, so page until the exact one
        return Paginator.findFirst(
                (max, offset) -> teamsApi().listOrganizationTeams(orgId, max, offset, teamName),
                ListTeamResponse::getTeams,
                ListTeamResponse::getTotalSize,
                item -> Objects.equals(item.getName(), teamName)
        ).orElseThrow(() -> new TeamNotFoundException(orgId, teamName));
    }

    protected TeamDbDto fetchTeam(Long orgId, TeamRefOptions ref) throws ApiException {
        Long teamId = ref.team.id != null ? ref.team.id : findTeamByName(orgId, ref.team.name).getTeamId();
        return teamsApi().describeOrganizationTeam(orgId, teamId).getTeam();
    }

    public void deleteTeamById(Long teamId, String orgRef) throws OrganizationNotFoundException, ApiException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(orgRef);
        deleteTeamById(teamId, orgAndWorkspaceDbDto.getOrgId());
    }

    public void deleteTeamById(Long teamId, Long orgId) throws OrganizationNotFoundException, ApiException {
        teamsApi().deleteOrganizationTeam(orgId, teamId);
    }
}
