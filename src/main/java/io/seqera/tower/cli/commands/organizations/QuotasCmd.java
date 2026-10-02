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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationQuotasView;
import io.seqera.tower.model.OrganizationDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "quotas",
        description = "View organization quotas"
)
public class QuotasCmd extends AbstractOrganizationsCmd {

    @Mixin
    OrganizationRefOptions organizationRefOptions;

    @Override
    protected Response exec() throws ApiException {
        OrganizationDbDto organization = fetchOrganization(organizationRefOptions).getOrganization();
        return new OrganizationQuotasView(organization.getName(), orgsApi().describeOrganizationQuotas(organization.getOrgId(), null).getQuotas());
    }
}
