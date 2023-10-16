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
    @CommandLine.Option(names = {"--operations", "-o"}, description = "Type of operation (set, append, delete) [default: set].", defaultValue = "set")
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
        set("set"),
        append("append"),
        delete("delete");

        public final String prettyName;

        Operation(String prettyName) {
            this.prettyName = prettyName;
        }
    }

}
