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

package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
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
        ListMembersResponse listMembersResponse = api().listOrganizationTeamMembers(orgId, teamId, 25, 0, username);

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
        ListTeamResponse listTeamResponse = api().listOrganizationTeams(orgId, null, null, null);

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

    public void deleteTeamById(Long teamId, String orgRef) throws OrganizationNotFoundException, ApiException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(orgRef);
        deleteTeamById(teamId, orgAndWorkspaceDbDto.getOrgId());
    }

    public void deleteTeamById(Long teamId, Long orgId) throws OrganizationNotFoundException, ApiException {
        api().deleteOrganizationTeam(orgId, teamId);
    }
}
