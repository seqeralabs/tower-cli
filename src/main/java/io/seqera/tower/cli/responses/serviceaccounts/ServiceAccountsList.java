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

package io.seqera.tower.cli.responses.serviceaccounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ServiceAccountDto;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class ServiceAccountsList extends Response {

    public final String organizationName;
    @Nullable
    public final String workspaceName;
    public final List<ServiceAccountDto> serviceAccounts;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public ServiceAccountsList(String organizationName, @Nullable String workspaceName, List<ServiceAccountDto> serviceAccounts, @Nullable PaginationInfo paginationInfo) {
        this.organizationName = organizationName;
        this.workspaceName = workspaceName;
        this.serviceAccounts = serviceAccounts;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(workspaceName == null
                ? String.format("%n  @|bold Service accounts for %s organization:|@%n", organizationName)
                : String.format("%n  @|bold Service accounts for '%s/%s' workspace:|@%n", organizationName, workspaceName)));

        if (serviceAccounts == null || serviceAccounts.isEmpty()) {
            out.println(ansi("    @|yellow No service accounts found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Name", "Description", "Created");
        table.setPrefix("    ");
        serviceAccounts.forEach(sa -> table.addRow(
                sa.getId().toString(),
                sa.getName(),
                sa.getDescription(),
                formatDate(sa.getCreatedAt())
        ));
        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
