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


package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.CredentialsSetupValue;

import java.io.PrintWriter;
import java.util.List;

public class CredentialsFederationSetup extends Response {

    public final String provider;
    public final String workspaceRef;
    public final List<CredentialsSetupValue> setupValues;

    public CredentialsFederationSetup(String provider, String workspaceRef, List<CredentialsSetupValue> setupValues) {
        this.provider = provider;
        this.workspaceRef = workspaceRef;
        this.setupValues = setupValues;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Workload identity setup values for '%s' credentials at %s workspace:|@%n", provider, workspaceRef)));
        if (setupValues == null || setupValues.isEmpty()) {
            out.println(ansi("    @|yellow No setup values for this provider|@"));
            return;
        }

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        setupValues.forEach(value -> table.addRow(value.getLabel(), value.getValue()));
        table.print();
        out.println("");
    }
}
