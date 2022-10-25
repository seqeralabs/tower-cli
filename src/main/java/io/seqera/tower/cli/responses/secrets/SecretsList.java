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
