/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
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
