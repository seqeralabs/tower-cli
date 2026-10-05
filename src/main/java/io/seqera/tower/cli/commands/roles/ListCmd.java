/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.roles;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.roles.RolesList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListRolesResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "list",
        description = "List predefined and custom roles"
)
public class ListCmd extends AbstractApiCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Option(names = {"-t", "--type"}, description = "Role type to list (predefined or custom).")
    public String type;

    @Option(names = {"-f", "--filter"}, description = "Show only roles whose name contains the given text (* wildcards allowed).")
    public String filter;

    @Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        ListRolesResponse response = rolesApi().listRoles(orgId, max, offset, filter, type);

        Long total = response.getTotalSize() == null ? null : response.getTotalSize().longValue();
        return new RolesList(organizationRef, response.getRoles(), PaginationInfo.from(paginationOptions, total));
    }
}
