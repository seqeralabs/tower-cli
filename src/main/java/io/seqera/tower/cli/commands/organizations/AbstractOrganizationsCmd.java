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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.model.DescribeOrganizationResponse;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command
public class AbstractOrganizationsCmd extends AbstractApiCmd {

    public AbstractOrganizationsCmd() {
    }

    protected OrgAndWorkspaceDto organizationByName(String organizationName) throws ApiException {
        ListWorkspacesAndOrgResponse workspacesAndOrgResponse = api().listWorkspacesUser(userId());

        if (workspacesAndOrgResponse.getOrgsAndWorkspaces() == null) {
            throw new OrganizationNotFoundException(organizationName);
        }

        List<OrgAndWorkspaceDto> orgAndWorkspaceDbDtoList = workspacesAndOrgResponse
                .getOrgsAndWorkspaces()
                .stream()
                .filter(
                        item -> Objects.equals(item.getOrgName(), organizationName)
                )
                .collect(Collectors.toList());

        if (orgAndWorkspaceDbDtoList.isEmpty()) {
            throw new OrganizationNotFoundException(organizationName);
        }

        return orgAndWorkspaceDbDtoList.stream().findFirst().orElse(null);
    }

    protected DescribeOrganizationResponse fetchOrganization(OrganizationRefOptions organizationRefOptions) throws ApiException {
        DescribeOrganizationResponse response;

        if(organizationRefOptions.organization.organizationId != null){
            response = api().describeOrganization(organizationRefOptions.organization.organizationId);
        } else {
            OrgAndWorkspaceDto orgAndWorkspaceDbDto = organizationByName(organizationRefOptions.organization.organizationName);
            response = api().describeOrganization(orgAndWorkspaceDbDto.getOrgId());
        }

        return response;
    }

    protected void deleteOrgById(Long orgId) throws OrganizationNotFoundException, ApiException {
        api().deleteOrganization(orgId);
    }

    protected void deleteOrgByName(String orgName) throws OrganizationNotFoundException, ApiException {
        OrgAndWorkspaceDto org = organizationByName(orgName);
        deleteOrgById(org.getOrgId());
    }
}
