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

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.model.CreateLabelRequest;
import io.seqera.tower.model.LabelDbDto;
import io.seqera.tower.model.ListLabelsResponse;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelsSubcmdOptions {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Parameters(split = ",", description = "a comma separated list of labels",converter = Label.LabelConverter.class)
    private List<Label> labels;
    @CommandLine.Option(names = "--no-create", description = "do not create labels if they not exist")
    private boolean noCreate;
    @CommandLine.Option(names = {"--operations", "-o"}, description = "selects the operation to do on labels, defaults to set", defaultValue = "set")
    private Operation operation;

    public String getOperationName() {
        return operation.prettyName;
    }

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
