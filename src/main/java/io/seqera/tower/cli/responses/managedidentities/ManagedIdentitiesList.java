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
import java.util.List;

public class ManagedIdentitiesList extends Response {

    public final String organizationRef;
    public final List<ManagedIdentityDbDtoAbstractGridConfig> managedIdentities;

    public ManagedIdentitiesList(String organizationRef, List<ManagedIdentityDbDtoAbstractGridConfig> managedIdentities) {
        this.organizationRef = organizationRef;
        this.managedIdentities = managedIdentities;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Managed identities at %s organization:|@%n", organizationRef)));
        if (managedIdentities == null || managedIdentities.isEmpty()) {
            out.println(ansi("    @|yellow No managed identities found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Name", "Platform", "Host").sortBy(0);
        table.setPrefix("    ");
        managedIdentities.forEach(it -> table.addRow(
                it.getId().toString(),
                it.getName(),
                it.getPlatform() != null ? it.getPlatform().getValue() : "",
                it.getConfig() != null ? String.format("%s:%s", it.getConfig().getHostName(), it.getConfig().getPort()) : ""
        ));
        table.print();
        out.println("");
    }
}
