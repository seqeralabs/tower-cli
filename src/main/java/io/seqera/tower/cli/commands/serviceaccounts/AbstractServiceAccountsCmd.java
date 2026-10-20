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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.ServiceAccountDto;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractServiceAccountsCmd extends AbstractApiCmd {

    public static final String ORGANIZATION_DESCRIPTION = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.";

    protected ServiceAccountDto fetchServiceAccount(String organizationRef, Long orgId, ServiceAccountRefOptions ref) throws ApiException {
        if (ref.serviceAccount.id != null) {
            return serviceAccountsApi().describeServiceAccount(orgId, ref.serviceAccount.id).getServiceAccount();
        }
        ServiceAccountDto sa = findServiceAccountByName(orgId, ref.serviceAccount.name);
        if (sa == null) {
            throw new TowerException(String.format("Service account '%s' not found in organization '%s'", ref.serviceAccount.name, organizationRef));
        }
        return sa;
    }
}
