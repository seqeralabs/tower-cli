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

package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Command
public abstract class AbstractWorkspaceCmd extends AbstractApiCmd {

    public AbstractWorkspaceCmd() {
    }
    
    protected OrgAndWorkspaceDbDto orgAndWorkspaceByName(String workspaceName, String organizationName) throws ApiException {
        return findOrgAndWorkspaceByName(organizationName, workspaceName).orElse(null);
    }

    protected OrgAndWorkspaceDbDto organizationByName(String organizationName) throws ApiException {
        return findOrgAndWorkspaceByName(organizationName, null).orElse(null);
    }

    protected OrgAndWorkspaceDbDto workspaceById(Long workspaceId) throws ApiException {
        return findOrgAndWorkspaceById(workspaceId).orElse(null);
    }

    private Optional<OrgAndWorkspaceDbDto> findOrgAndWorkspaceByName(String organizationName, String workspaceName) throws ApiException {
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

        return orgAndWorkspaceDbDtoList.stream().findFirst();
    }
    private Optional<OrgAndWorkspaceDbDto> findOrgAndWorkspaceById(Long workspaceId) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse == null || workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new WorkspaceNotFoundException(workspaceId);
        }

        List<OrgAndWorkspaceDbDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getWorkspaceId(), workspaceId)
                )
                .collect(Collectors.toList());

        if (orgAndWorkspaceDbDtoList.isEmpty()) {
            throw new WorkspaceNotFoundException(workspaceId);
        }

        return orgAndWorkspaceDbDtoList.stream().findFirst();
    }
}


