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

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ServiceAccountDto;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class ServiceAccountView extends Response {

    public final String organizationName;
    public final ServiceAccountDto serviceAccount;

    public ServiceAccountView(String organizationName, ServiceAccountDto serviceAccount) {
        this.organizationName = organizationName;
        this.serviceAccount = serviceAccount;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Service account at %s organization:|@%n", organizationName)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", serviceAccount.getId().toString());
        table.addRow("Name", serviceAccount.getName());
        table.addRow("Description", serviceAccount.getDescription());
        table.addRow("Member ID", serviceAccount.getMemberId() == null ? null : serviceAccount.getMemberId().toString());
        table.addRow("Created", formatDate(serviceAccount.getCreatedAt()));
        table.print();
        out.println("");
    }
}
