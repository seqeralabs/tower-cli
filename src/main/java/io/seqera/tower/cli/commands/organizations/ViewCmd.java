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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsView;
import io.seqera.tower.model.DescribeOrganizationResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "view",
        description = "Describe organization details."
)
public class ViewCmd extends AbstractOrganizationsCmd {

    @CommandLine.Mixin
    OrganizationRefOptions organizationRefOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        DescribeOrganizationResponse response = fetchOrganization(organizationRefOptions);

        return new OrganizationsView(response.getOrganization());
    }
}
