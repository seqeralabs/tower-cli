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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.ListServiceAccountsResponse;
import io.seqera.tower.model.ServiceAccountDto;
import picocli.CommandLine.Command;

import java.util.List;

@Command
public abstract class AbstractServiceAccountsCmd extends AbstractApiCmd {

    public static final String ORGANIZATION_DESCRIPTION = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.";

    protected ServiceAccountDto fetchServiceAccount(Long orgId, ServiceAccountRefOptions ref) throws ApiException {
        if (ref.serviceAccount.id != null) {
            return serviceAccountsApi().describeServiceAccount(orgId, ref.serviceAccount.id).getServiceAccount();
        }
        return serviceAccountByName(orgId, ref.serviceAccount.name);
    }

    private ServiceAccountDto serviceAccountByName(Long orgId, String name) throws ApiException {
        int offset = 0;
        while (true) {
            ListServiceAccountsResponse page = serviceAccountsApi().listServiceAccounts(orgId, offset, PaginationOptions.MAX);
            List<ServiceAccountDto> items = page.getServiceAccounts();
            for (ServiceAccountDto sa : items) {
                if (name.equals(sa.getName())) {
                    return sa;
                }
            }
            offset += items.size();
            if (items.isEmpty() || offset >= page.getTotalSize()) {
                throw new TowerException(String.format("Service account '%s' not found in organization '%d'", name, orgId));
            }
        }
    }
}
