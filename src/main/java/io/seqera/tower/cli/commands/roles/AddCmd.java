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
import io.seqera.tower.cli.responses.roles.RoleAdded;
import io.seqera.tower.model.CreateRoleRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(
        name = "add",
        description = "Add a custom role"
)
public class AddCmd extends AbstractApiCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Option(names = {"-n", "--name"}, description = "Custom role name. Must be unique within the organization and different from the predefined role names.", required = true)
    public String name;

    @Option(names = {"-d", "--description"}, description = "Custom role description (max 120 characters).", required = true)
    public String description;

    @Option(names = {"-p", "--permissions"}, split = ",", description = "Comma-separated list of permissions granted by the role. See 'tw roles permissions' for the available names.", required = true)
    public List<String> permissions;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();

        CreateRoleRequest request = new CreateRoleRequest()
                .name(name)
                .description(description)
                .permissions(permissions);

        rolesApi().createRole(request, orgId);

        return new RoleAdded(organizationRef, name);
    }
}
