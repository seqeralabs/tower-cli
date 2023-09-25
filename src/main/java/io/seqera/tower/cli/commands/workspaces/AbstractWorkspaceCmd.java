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
import io.seqera.tower.cli.exceptions.TowerException;
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
        return findOrgAndWorkspaceByName(organizationName, workspaceName);
    }

    protected OrgAndWorkspaceDbDto organizationByName(String organizationName) throws ApiException {
        return findOrgAndWorkspaceByName(organizationName, null);
    }

    protected OrgAndWorkspaceDbDto workspaceById(Long workspaceId) throws ApiException {
        return findOrgAndWorkspaceById(workspaceId).orElse(null);
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

    protected OrgAndWorkspaceDbDto fetchOrgAndWorkspaceDbDto(WorkspaceRefOptions workspaceRefOptions) throws ApiException {
        OrgAndWorkspaceDbDto ws;

        if (workspaceRefOptions.workspace.workspaceId != null) {
            ws = workspaceById(workspaceRefOptions.workspace.workspaceId);
        } else {
            if (workspaceRefOptions.workspace.workspaceName.contains(WORKSPACE_REF_SEPARATOR)) {
                String[] wspRef = workspaceRefOptions.workspace.workspaceName.split(WORKSPACE_REF_SEPARATOR);
                ws = findOrgAndWorkspaceByName(wspRef[0], wspRef[1]);
            } else {
                throw new TowerException("Invalid workspace namespace");
            }
        }

        return ws;
    }

    protected void deleteWorkspaceById(Long wspId, Long orgId) throws WorkspaceNotFoundException, ApiException {
        api().deleteWorkspace(orgId, wspId);
    }
}


