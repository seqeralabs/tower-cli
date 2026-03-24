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

package io.seqera.tower.cli.responses.secrets;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineSecret;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class SecretsList extends Response {

    public final String workspaceRef;
    public final List<PipelineSecret> secrets;

    public SecretsList(String workspaceRef, List<PipelineSecret> secrets) {
        this.workspaceRef = workspaceRef;
        this.secrets = secrets;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Secrets at %s workspace:|@%n", workspaceRef)));
        if (secrets == null || secrets.isEmpty()) {
            out.println(ansi("    @|yellow No secrets found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Name", "Created", "Updated", "Used").sortBy(0);
        table.setPrefix("    ");
        secrets.forEach(secret -> table.addRow(
                secret.getId().toString(),
                secret.getName(),
                formatDate(secret.getDateCreated()),
                formatDate(secret.getLastUpdated()),
                formatDate(secret.getLastUsed())
        ));
        table.print();
        out.println("");
    }


}
