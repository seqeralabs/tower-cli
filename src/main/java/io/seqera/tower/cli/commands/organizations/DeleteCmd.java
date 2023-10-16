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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete an organization."
)
public class DeleteCmd extends AbstractOrganizationsCmd {

    @CommandLine.Mixin
    OrganizationRefOptions organizationRefOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long id;
        String ref;

        if(organizationRefOptions.organization.organizationId != null){
            id = organizationRefOptions.organization.organizationId;
            ref = id.toString();
        } else {
            OrgAndWorkspaceDto orgAndWorkspaceDbDto = organizationByName(organizationRefOptions.organization.organizationName);
            id = orgAndWorkspaceDbDto.getOrgId();
            ref = orgAndWorkspaceDbDto.getOrgName();
        }

        try {
            deleteOrgById(id);
        } catch (Exception e) {
            throw new TowerException(String.format("Organization %s could not be deleted", ref));
        }

        return new OrganizationsDeleted(ref);
    }
}
