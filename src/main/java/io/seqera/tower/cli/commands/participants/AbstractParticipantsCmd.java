/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.ParticipantNotFoundException;
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.ListParticipantsResponse;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.util.Objects;

@CommandLine.Command
public class AbstractParticipantsCmd extends AbstractApiCmd {

    public AbstractParticipantsCmd() {
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
        ListTeamResponse listTeamResponse = api().listOrganizationTeams(orgId, null, null, null);

        if (listTeamResponse.getTeams() != null) {
            TeamDbDto team = listTeamResponse.getTeams().stream().filter(it -> Objects.equals(it.getName(), name)).findFirst().orElse(null);

            if (team != null) {
                return team;
            }
        }

        throw new TeamNotFoundException(orgId, name);
    }

    protected ParticipantDbDto findWorkspaceParticipant(Long organizationId, Long workspaceId, String name, ParticipantType type) throws ApiException {
        ListParticipantsResponse listParticipantsResponse = api().listWorkspaceParticipants(organizationId, workspaceId, null, null, name);

        if (listParticipantsResponse.getParticipants() != null) {
            ParticipantDbDto participant = listParticipantsResponse.getParticipants().stream().filter(it -> it.getType() == type).findFirst().orElse(null);

            if (participant != null) {
                return participant;
            }
        }

        throw new ParticipantNotFoundException(workspaceId, name);
    }

    protected void deleteParticipantByNameAndType(Long wspId, String participantName, ParticipantType type) throws ParticipantNotFoundException, ApiException {
        ParticipantDbDto participant = findWorkspaceParticipant(orgId(wspId), wspId, participantName, type);
        api().deleteWorkspaceParticipant(orgId(wspId), wspId, participant.getParticipantId());
    }
}
