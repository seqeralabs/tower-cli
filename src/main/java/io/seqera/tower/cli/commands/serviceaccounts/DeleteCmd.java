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
import io.seqera.tower.cli.exceptions.TowerException;
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
        ServiceAccountDto sa = fetchServiceAccount(organizationRef, orgId, ref);

        try {
            serviceAccountsApi().deleteServiceAccount(orgId, sa.getId());
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                throw new TowerException(String.format("Service account '%s' is bound to active agents. Bind those agents to another service account with 'tw agents update --service-account', or disable them with 'tw agents disable', then try again", sa.getName()));
            }
            if (e.getCode() == 502) {
                throw new TowerException(String.format("Service account '%s' was not deleted: the agent service is unreachable, so its agent bindings could not be checked. Try again later", sa.getName()));
            }
            throw e;
        }

        return new ServiceAccountDeleted(organizationRef, sa);
    }
}
