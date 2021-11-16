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

package io.seqera.tower.cli.commands.collaborators;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.collaborators.CollaboratorsList;
import io.seqera.tower.model.ListMembersResponse;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        description = "List all the collaborators of a given organization."
)
public class ListCmd extends AbstractCollaboratorsCmd {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization identifier.", required = true)
    public Long organizationId;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Only show members with usernames that start with the given word.")
    public String startsWith;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListMembersResponse response = api().listOrganizationCollaborators(organizationId, max, offset, startsWith);

        return new CollaboratorsList(organizationId, response.getMembers());
    }
}
