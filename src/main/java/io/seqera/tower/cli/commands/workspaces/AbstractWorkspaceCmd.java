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

package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Command
public abstract class AbstractWorkspaceCmd extends AbstractApiCmd {

    public AbstractWorkspaceCmd() {
    }

    protected OrgAndWorkspaceDto orgAndWorkspaceByName(String workspaceName, String organizationName) throws ApiException {
        return findOrgAndWorkspaceByName(organizationName, workspaceName);
    }

    protected OrgAndWorkspaceDto organizationByName(String organizationName) throws ApiException {
        return findOrgAndWorkspaceByName(organizationName, null);
    }

    protected OrgAndWorkspaceDto workspaceById(Long workspaceId) throws ApiException {
        return findOrgAndWorkspaceById(workspaceId).orElse(null);
    }

    private Optional<OrgAndWorkspaceDto> findOrgAndWorkspaceById(Long workspaceId) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = workspacesApi().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse == null || workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new WorkspaceNotFoundException(workspaceId);
        }

        List<OrgAndWorkspaceDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
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

    protected OrgAndWorkspaceDto fetchOrgAndWorkspaceDbDto(WorkspaceRefOptions workspaceRefOptions) throws ApiException {
        OrgAndWorkspaceDto ws;

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
        workspacesApi().deleteWorkspace(orgId, wspId);
    }
}
