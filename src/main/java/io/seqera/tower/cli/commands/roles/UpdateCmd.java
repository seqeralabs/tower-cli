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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.roles.RoleUpdated;
import io.seqera.tower.model.RoleDto;
import io.seqera.tower.model.UpdateRoleRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(
        name = "update",
        description = "Update a custom role"
)
public class UpdateCmd extends AbstractApiCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Option(names = {"-n", "--name"}, description = "Custom role name.", required = true)
    public String name;

    @Option(names = {"--new-name"}, description = "New custom role name.")
    public String newName;

    @Option(names = {"-d", "--description"}, description = "New custom role description (max 120 characters).")
    public String description;

    @Option(names = {"-p", "--permissions"}, split = ",", description = "Comma-separated list of permissions. Replaces the current permissions of the role.")
    public List<String> permissions;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        RoleDto role = rolesApi().describeRole(name, orgId).getRole();

        // The API replaces name, description and permissions, so unset options keep the current values
        UpdateRoleRequest request = new UpdateRoleRequest()
                .name(newName != null ? newName : role.getName())
                .description(description != null ? description : role.getDescription())
                .permissions(permissions != null ? permissions : role.getPermissions());

        rolesApi().updateRole(name, request, orgId);

        return new RoleUpdated(organizationRef, request.getName());
    }
}
