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


package io.seqera.tower.cli.responses.labels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.LabelDbDto;
import io.seqera.tower.model.LabelType;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.List;

public class ListLabelsCmdResponse extends Response {

    public Long workspaceId;
    public List<LabelDbDto> labels;

    public LabelType labelType;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public ListLabelsCmdResponse(Long wspId, LabelType labelType, List<LabelDbDto> labels, @Nullable PaginationInfo paginationInfo) {
        this.workspaceId = wspId;
        this.labels = labels;
        this.labelType = labelType;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {

        if (workspaceId != null) {
            out.println(ansi(String.format("%n  @|bold Labels at %d workspace:|@%n", workspaceId)));
        } else {
            out.println(ansi(String.format("%n  @|bold Labels in user workspace:|@%n")));
        }

        if (labels.isEmpty()) {
            out.println(ansi("    @|yellow No labels found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "  ID", "Name", "Value", "Type").sortBy(0);
        table.setPrefix("    ");
        labels.forEach(label -> table.addRow(
                label.getId().toString(),
                label.getName(),
                label.getValue(),
                label.getResource() ? "Resource" : "Normal"
        ));

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }

}
