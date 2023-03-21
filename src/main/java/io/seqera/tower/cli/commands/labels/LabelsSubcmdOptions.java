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

import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import picocli.CommandLine;

import java.util.List;

public class LabelsSubcmdOptions {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Parameters(split = ",", description = "List of labels seperated by coma.", converter = Label.LabelConverter.class)
    private List<Label> labels;
    @CommandLine.Option(names = "--no-create", description = "Assign labels without creating the ones which were not found.")
    private boolean noCreate;
    @CommandLine.Option(names = {"--operations", "-o"}, description = "Type of operation (set, append, remove) [default: set].", defaultValue = "set")
    private Operation operation;

    public Operation getOperation() {
        return operation;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public boolean getNoCreate() {
        return noCreate;
    }


    public enum Operation {
        set("Applied"),
        append("Added"),
        delete("Removed");

        public final String prettyName;

        Operation(String prettyName) {
            this.prettyName = prettyName;
        }
    }

}
