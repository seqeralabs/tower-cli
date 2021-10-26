package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.TeamsCmd;
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
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Command
public abstract class AbstractTeamsCmd extends AbstractApiCmd {

    @ParentCommand
    protected TeamsCmd parent;

    public AbstractTeamsCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
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
