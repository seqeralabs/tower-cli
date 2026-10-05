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
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountAdded;
import io.seqera.tower.model.CreateServiceAccountRequest;
import io.seqera.tower.model.ServiceAccountDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "add",
        description = "Add a service account"
)
public class AddCmd extends AbstractServiceAccountsCmd {

    @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION, required = true)
    public String organizationRef;

    @Option(names = {"-n", "--name"}, description = "Service account name. Must be unique across all Platform users. Use only lowercase letters, numbers, and dashes (max 40 characters).", required = true)
    public String name;

    @Option(names = {"-d", "--description"}, description = "Service account description (max 1000 characters).")
    public String description;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();

        CreateServiceAccountRequest request = new CreateServiceAccountRequest()
                .name(name)
                .description(description);

        ServiceAccountDto sa = serviceAccountsApi().createServiceAccount(orgId, request).getServiceAccount();

        return new ServiceAccountAdded(organizationRef, sa);
    }
}
