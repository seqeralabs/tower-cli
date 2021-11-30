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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
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
            OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(organizationRefOptions.organization.organizationName);
            id = orgAndWorkspaceDbDto.getOrgId();
            ref = orgAndWorkspaceDbDto.getOrgName();
        }

        try {
            api().deleteOrganization(id);
        } catch (Exception e) {
            throw new TowerException(String.format("Organization %s could not be deleted", ref));
        }

        return new OrganizationsDeleted(ref);
    }
}
