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

import java.io.PrintWriter;
import java.util.List;

public class ComponentVersions extends Response {

    public record ComponentVersion(String version, boolean isDefault) {
    }

    public final String component;
    public final List<ComponentVersion> versions;

    public ComponentVersions(String component, List<ComponentVersion> versions) {
        this.component = component;
        this.versions = versions;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Available %s versions:|@%n", component)));
        if (versions.isEmpty()) {
            out.println(ansi(String.format("    @|yellow No %s versions found|@", component)));
            return;
        }

        TableList table = new TableList(out, 2, "Version", "Default");
        table.setPrefix("    ");
        versions.forEach(v -> table.addRow(v.version(), v.isDefault() ? "yes" : "no"));
        table.print();
        out.println("");
    }
}
