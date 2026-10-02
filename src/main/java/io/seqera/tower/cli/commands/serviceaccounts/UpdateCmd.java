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

package io.seqera.tower.cli.commands.serviceaccounts;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountUpdated;
import io.seqera.tower.model.ServiceAccountDto;
import io.seqera.tower.model.UpdateServiceAccountRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "update",
        description = "Update a service account"
)
public class UpdateCmd extends AbstractServiceAccountsCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Mixin
    ServiceAccountRefOptions ref;

    @Option(names = {"--new-name"}, description = "New service account name. Must be unique across all Platform users.")
    public String newName;

    @Option(names = {"-d", "--description"}, description = "New service account description (max 1000 characters).")
    public String description;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        ServiceAccountDto sa = fetchServiceAccount(orgId, ref);

        // PATCH semantics: fields left null are not changed
        UpdateServiceAccountRequest request = new UpdateServiceAccountRequest()
                .name(newName)
                .description(description);

        ServiceAccountDto updated = serviceAccountsApi().updateServiceAccount(orgId, sa.getId(), request).getServiceAccount();

        return new ServiceAccountUpdated(organizationRef, updated);
    }
}
