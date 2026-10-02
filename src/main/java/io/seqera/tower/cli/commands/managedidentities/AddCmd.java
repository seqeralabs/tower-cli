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


package io.seqera.tower.cli.commands.managedidentities;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityAdded;
import io.seqera.tower.model.CreateManagedIdentityRequest;
import io.seqera.tower.model.CreateManagedIdentityResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "add",
        description = "Add a managed identity for an HPC cluster. Each organization member then adds their own SSH credentials with 'tw managed-identities credentials add'."
)
public class AddCmd extends AbstractManagedIdentitiesCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Option(names = {"-n", "--name"}, description = "Cluster name. Must be unique per organization. Names consist of alphanumeric, hyphen, and underscore characters.", required = true)
    public String name;

    @Option(names = {"-p", "--platform"}, description = "Cluster workload manager: ${COMPLETION-CANDIDATES}.", required = true)
    public ManagedIdentityPlatform platform;

    @Option(names = {"-H", "--host-name"}, description = "Hostname of the cluster to connect to via SSH, usually the login node. Must be a fully qualified hostname, not a local IP address.", required = true)
    public String hostName;

    @Option(names = {"--port"}, description = "SSH port for the login connection [default: 22].", defaultValue = "22")
    public Integer port;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        CreateManagedIdentityRequest request = new CreateManagedIdentityRequest()
                .name(name)
                .platform(CreateManagedIdentityRequest.PlatformEnum.fromValue(platform.value))
                .config(platform.config(hostName, port));
        CreateManagedIdentityResponse response = identitiesApi().createManagedIdentity(request, orgId);
        return new ManagedIdentityAdded(organizationRef, response.getId(), response.getName());
    }
}
