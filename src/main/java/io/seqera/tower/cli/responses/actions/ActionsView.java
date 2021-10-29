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

import java.io.PrintWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.ModelHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Action;
import io.seqera.tower.model.WorkflowLaunchRequest;

public class ActionsView extends Response {

    final public Action action;

    public ActionsView(Action action) {
        this.action = action;
    }

    @Override
    public void toString(PrintWriter out) {
        String configJson = "";
        try {
            WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(action.getLaunch());
            configJson = new JSON().getContext(WorkflowLaunchRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        out.println(ansi(String.format("%n  @|bold Details for action '%s'|@%n", action.getName())));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", action.getId());
        table.addRow("Name", action.getName());
        table.addRow("Pipeline URL", action.getLaunch().getPipeline());
        table.addRow("Source", action.getSource().toString());
        table.addRow("Hook URL", action.getHookUrl());
        table.addRow("Last event", formatTime(action.getLastSeen()));
        table.addRow("Date created", formatTime(action.getDateCreated()));
        table.addRow("Last event", formatTime(action.getLastSeen()));
        table.print();

        out.println(String.format("%n  Configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));

    }
}
