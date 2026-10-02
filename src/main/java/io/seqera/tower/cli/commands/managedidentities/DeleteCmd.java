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
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityDeleted;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "delete",
        description = "Delete a managed identity and all its members' credentials. Compute environments that use it become invalid."
)
public class DeleteCmd extends AbstractManagedIdentitiesCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Mixin
    ManagedIdentityRefOptions ref;

    @Option(names = {"--force"}, description = "Delete the managed identity even if running jobs use its credentials. Those jobs are stopped. By default, a managed identity in use is not deleted.")
    public boolean force = false;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        ManagedIdentityDbDtoAbstractGridConfig identity = findManagedIdentity(orgId, ref);
        deleteUnlessInUse(identity.getName(), () -> identitiesApi().deleteManagedIdentity(identity.getId(), orgId, !force));
        return new ManagedIdentityDeleted(organizationRef, identity.getId(), identity.getName());
    }
}
