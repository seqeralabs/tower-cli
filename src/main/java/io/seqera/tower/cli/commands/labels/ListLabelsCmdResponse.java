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


package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.LabelDbDto;

import java.io.PrintWriter;
import java.util.List;

public class ListLabelsCmdResponse extends Response {

    public Long workspaceId;
    public List<LabelDbDto> labels;

    public ListLabelsCmdResponse(Long wspId, List<LabelDbDto> labels) {
        this.workspaceId = wspId;
        this.labels = labels;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Labels at %d workspace:|@%n", workspaceId)));

        if (labels.isEmpty()) {
            out.println(ansi("    @|yellow No labels found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "  ID", "Name", "Value").sortBy(0);
        table.setPrefix("    ");
        labels.forEach(label -> table.addRow(
                label.getId().toString(),
                label.getName(),
                label.getValue()
        ));
        table.print();
        out.println("");

    }

}
