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


package io.seqera.tower.cli.responses.managedidentities;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListManagedCredentialsRespDto;

import java.io.PrintWriter;
import java.util.List;

public class ManagedCredentialsList extends Response {

    public final String organizationRef;
    public final String managedIdentityName;
    public final List<ListManagedCredentialsRespDto> managedCredentials;

    public ManagedCredentialsList(String organizationRef, String managedIdentityName, List<ListManagedCredentialsRespDto> managedCredentials) {
        this.organizationRef = organizationRef;
        this.managedIdentityName = managedIdentityName;
        this.managedCredentials = managedCredentials;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Credentials of managed identity '%s' at %s organization:|@%n", managedIdentityName, organizationRef)));
        if (managedCredentials == null || managedCredentials.isEmpty()) {
            out.println(ansi("    @|yellow No credentials found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Member", "Name", "Status");
        table.setPrefix("    ");
        managedCredentials.forEach(it -> table.addRow(
                it.getManagedCredentialsId() != null ? it.getManagedCredentialsId().toString() : "",
                it.getUserName(),
                String.join(" ", it.getFirstName() != null ? it.getFirstName() : "", it.getLastName() != null ? it.getLastName() : "").strip(),
                it.getManagedCredentialsId() != null ? "added" : "missing"
        ));
        table.print();
        out.println("");
    }
}
