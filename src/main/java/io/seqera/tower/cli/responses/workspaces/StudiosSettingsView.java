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

package io.seqera.tower.cli.responses.workspaces;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataStudioWorkspaceSettingsResponse;

import java.io.PrintWriter;

public class StudiosSettingsView extends Response {

    public final String workspaceName;
    public final DataStudioWorkspaceSettingsResponse settings;

    public StudiosSettingsView(String workspaceName, DataStudioWorkspaceSettingsResponse settings) {
        this.workspaceName = workspaceName;
        this.settings = settings;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Studios settings for workspace '%s'|@%n", workspaceName)));

        TableList table = new TableList(out, 2);
        table.setPrefix(" ");
        table.addRow("Container repository", display(settings.getContainerRepository()));
        table.addRow("Lifespan (hours)", display(settings.getLifespanHours()));
        table.addRow("Name strategy", display(settings.getNameStrategy()));
        table.addRow("Private studio by default", display(settings.getPrivateStudioByDefault()));
        table.print();
        out.println("");
    }

    private static String display(Object value) {
        return value == null ? "-" : value.toString();
    }
}
