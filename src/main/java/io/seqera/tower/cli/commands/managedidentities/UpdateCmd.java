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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityUpdated;
import io.seqera.tower.model.AbstractGridConfig;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import io.seqera.tower.model.UpdateManagedIdentityRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "update",
        description = "Update a managed identity. Changing the host of a cluster used by compute environments may break them."
)
public class UpdateCmd extends AbstractManagedIdentitiesCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Mixin
    ManagedIdentityRefOptions ref;

    @Option(names = {"--new-name"}, description = "New cluster name. Must be unique per organization.")
    public String newName;

    @Option(names = {"-H", "--host-name"}, description = "New hostname of the cluster to connect to via SSH.")
    public String hostName;

    @Option(names = {"--port"}, description = "New SSH port for the login connection.")
    public Integer port;

    @Override
    protected Response exec() throws ApiException {
        if (newName == null && hostName == null && port == null) {
            throw new TowerException("Nothing to update: provide at least one of '--new-name', '--host-name' or '--port'");
        }

        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        ManagedIdentityDbDtoAbstractGridConfig current = findManagedIdentity(orgId, ref);
        AbstractGridConfig currentConfig = current.getConfig();

        // Platform only applies the name, host and port, but needs the platform to read the config
        ManagedIdentityDbDtoAbstractGridConfig update = new ManagedIdentityDbDtoAbstractGridConfig()
                .name(newName != null ? newName : current.getName())
                .platform(current.getPlatform())
                .config(ManagedIdentityPlatform.fromValue(current.getPlatform().getValue()).config(
                        hostName != null ? hostName : currentConfig.getHostName(),
                        port != null ? port : currentConfig.getPort()));
        identitiesApi().updateManagedIdentity(current.getId(), new UpdateManagedIdentityRequest().managedIdentity(update), orgId);

        return new ManagedIdentityUpdated(organizationRef, current.getId(), update.getName());
    }
}
