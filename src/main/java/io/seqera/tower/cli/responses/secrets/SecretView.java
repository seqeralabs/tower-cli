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

package io.seqera.tower.cli.responses.secrets;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineSecret;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class SecretView extends Response {

    public final String workspaceRef;
    public final PipelineSecret secret;

    public SecretView(String workspaceRef, PipelineSecret secret) {
        this.workspaceRef = workspaceRef;
        this.secret = secret;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Secret at %s workspace:|@%n", workspaceRef)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", secret.getId().toString());
        table.addRow("Name", secret.getName());
        table.addRow("Created", formatDate(secret.getDateCreated()));
        table.addRow("Updated", formatDate(secret.getLastUpdated()));
        table.addRow("Used", formatDate(secret.getLastUsed()));
        table.print();
        out.println("");
    }
}
