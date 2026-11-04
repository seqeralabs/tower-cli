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

package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.ParticipantNotFoundException;
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
import io.seqera.tower.cli.utils.Paginator;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.ListParticipantsResponse;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.MemberDbDto;   
import io.seqera.tower.model.ParticipantResponseDto;
import io.seqera.tower.model.ParticipantType;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.util.Objects;

@CommandLine.Command
public class AbstractParticipantsCmd extends AbstractApiCmd {

    public AbstractParticipantsCmd() {
    }

    // The list endpoints search usernames, emails, names and team names by substring, so every lookup below
    // pages through the results and keeps only an exact match. Taking the first result could pick 'aidan' for 'dan'.
    protected MemberDbDto findOrganizationMemberByName(Long orgId, String name) throws ApiException {
        return Paginator.findFirst(
                (max, offset) -> orgsApi().listOrganizationMembers(orgId, max, offset, name),
                ListMembersResponse::getMembers,
                ListMembersResponse::getTotalSize,
                it -> isUser(it.getUserName(), it.getEmail(), name)
        ).orElseThrow(() -> new MemberNotFoundException(orgId, name));
    }

    protected MemberDbDto findOrganizationCollaboratorByName(Long orgId, String name) throws ApiException {
        return Paginator.findFirst(
                (max, offset) -> orgsApi().listOrganizationCollaborators(orgId, max, offset, name),
                ListMembersResponse::getMembers,
                ListMembersResponse::getTotalSize,
                it -> isUser(it.getUserName(), it.getEmail(), name)
        ).orElseThrow(() -> new MemberNotFoundException(orgId, name));
    }

    protected TeamDbDto findOrganizationTeamByName(Long orgId, String name) throws ApiException {
        return Paginator.findFirst(
                (max, offset) -> teamsApi().listOrganizationTeams(orgId, max, offset, name),
                ListTeamResponse::getTeams,
                ListTeamResponse::getTotalSize,
                it -> Objects.equals(it.getName(), name)
        ).orElseThrow(() -> new TeamNotFoundException(orgId, name));
    }

    protected ParticipantResponseDto findWorkspaceParticipant(Long organizationId, Long workspaceId, String name, ParticipantType type) throws ApiException {
        return Paginator.findFirst(
                (max, offset) -> workspacesApi().listWorkspaceParticipants(organizationId, workspaceId, max, offset, name),
                ListParticipantsResponse::getParticipants,
                ListParticipantsResponse::getTotalSize,
                it -> it.getType() == type && (Objects.equals(it.getTeamName(), name) || isUser(it.getUserName(), it.getEmail(), name))
        ).orElseThrow(() -> new ParticipantNotFoundException(workspaceId, name));
    }

    private static boolean isUser(String userName, String email, String name) {
        return name.equalsIgnoreCase(userName) || name.equalsIgnoreCase(email);
    }

    protected void deleteParticipantByNameAndType(Long wspId, String participantName, ParticipantType type) throws ParticipantNotFoundException, ApiException {
        ParticipantResponseDto participant = findWorkspaceParticipant(orgId(wspId), wspId, participantName, type);
        workspacesApi().deleteWorkspaceParticipant(orgId(wspId), wspId, participant.getParticipantId());
    }
}
