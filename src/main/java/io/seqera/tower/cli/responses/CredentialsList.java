/*
 * Copyright 2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Credentials;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatCredentialsId;
import static io.seqera.tower.cli.utils.FormatHelper.formatTime;

public class CredentialsList extends Response {

    public final String workspaceRef;
    public final List<Credentials> credentials;

    @JsonIgnore
    private String baseWorkspaceUrl;

    public CredentialsList(String workspaceRef, List<Credentials> credentials, String baseWorkspaceUrl) {
        this.workspaceRef = workspaceRef;
        this.credentials = credentials;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Credentials at %s workspace:|@%n", workspaceRef)));

        if (credentials.isEmpty()) {
            out.println(ansi("    @|yellow No credentials found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Provider", "Name", "Last activity").sortBy(0);
        table.setPrefix("    ");
        credentials.forEach(element -> table.addRow(
                formatCredentialsId(element.getId(), baseWorkspaceUrl),
                element.getProvider().getValue(),
                element.getName(),
                formatTime(element.getLastUsed())
        ));

        table.print();
        out.println("");
    }
}
