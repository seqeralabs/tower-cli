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
