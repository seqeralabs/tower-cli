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

package io.seqera.tower.cli.responses.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListActionsResponseActionInfo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.FormatHelper.formatActionId;
import static io.seqera.tower.cli.utils.FormatHelper.formatActionStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;

public class ActionsList extends Response {

    public final String userName;
    public final List<ListActionsResponseActionInfo> actions;

    @JsonIgnore
    private String baseWorkspaceUrl;

    @JsonIgnore
    private boolean includeLabels;

    public ActionsList(List<ListActionsResponseActionInfo> actions, String userName, String baseWorkspaceUrl, boolean includeLabels) {
        this.userName = userName;
        this.actions = actions;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.includeLabels = includeLabels;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Actions for %s user:|@%n", userName)));

        if (actions.isEmpty()) {
            out.println(ansi("    @|yellow No actions found|@"));
            return;
        }

        List<String> descriptions = new ArrayList<>(List.of("ID", "Name", "Endpoint", "Status", "Source"));
        if (includeLabels) descriptions.add("Labels");

        String[] desc = new String[descriptions.size()];
        descriptions.toArray(desc);

        TableList table = new TableList(out, desc.length, desc).sortBy(0);
        table.setPrefix("    ");

        actions.forEach(element -> {

            List<String> rows = new ArrayList<>(List.of(
                    formatActionId(element.getId(), baseWorkspaceUrl),
                    element.getName(),
                    element.getEndpoint(),
                    formatActionStatus(element.getStatus()),
                    element.getSource().toString()
            ));
            if (includeLabels) rows.add(formatLabels(element.getLabels()));

            String[] rowsArray = new String[rows.size()];
            rows.toArray(rowsArray);
            table.addRow(rowsArray);
        });

        table.print();
        out.println("");
    }

}
