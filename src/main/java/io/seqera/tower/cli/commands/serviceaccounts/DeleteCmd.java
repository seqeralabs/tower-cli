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
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountDeleted;
import io.seqera.tower.model.ServiceAccountDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "delete",
        description = "Delete a service account"
)
public class DeleteCmd extends AbstractServiceAccountsCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Mixin
    ServiceAccountRefOptions ref;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        ServiceAccountDto sa = fetchServiceAccount(orgId, ref);

        serviceAccountsApi().deleteServiceAccount(orgId, sa.getId());

        return new ServiceAccountDeleted(organizationRef, sa);
    }
}
