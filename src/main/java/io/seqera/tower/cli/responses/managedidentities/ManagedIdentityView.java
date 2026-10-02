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
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;

import java.io.PrintWriter;

public class ManagedIdentityView extends Response {

    public final String organizationRef;
    public final ManagedIdentityDbDtoAbstractGridConfig managedIdentity;

    public ManagedIdentityView(String organizationRef, ManagedIdentityDbDtoAbstractGridConfig managedIdentity) {
        this.organizationRef = organizationRef;
        this.managedIdentity = managedIdentity;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Managed identity at %s organization:|@%n", organizationRef)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", managedIdentity.getId().toString());
        table.addRow("Name", managedIdentity.getName());
        table.addRow("Platform", managedIdentity.getPlatform() != null ? managedIdentity.getPlatform().getValue() : "");
        if (managedIdentity.getConfig() != null) {
            table.addRow("Host name", managedIdentity.getConfig().getHostName());
            table.addRow("Port", String.valueOf(managedIdentity.getConfig().getPort()));
        }
        table.print();
        out.println("");
    }
}
