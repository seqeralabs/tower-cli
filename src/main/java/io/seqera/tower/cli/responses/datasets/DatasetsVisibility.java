/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.responses.datasets;

import io.seqera.tower.cli.responses.Response;

import java.io.PrintWriter;
import java.util.List;

public class DatasetsVisibility extends Response {

    public final List<String> datasetIds;
    public final String workspace;
    public final boolean hidden;

    public DatasetsVisibility(List<String> datasetIds, String workspace, boolean hidden) {
        this.datasetIds = datasetIds;
        this.workspace = workspace;
        this.hidden = hidden;
    }

    @Override
    public void toString(PrintWriter out) {
        String action = hidden ? "hidden" : "shown";
        out.println(ansi(String.format("%n  @|yellow Datasets %s at %s workspace:|@", action, workspace)));
        datasetIds.forEach(id -> out.println(ansi(String.format("    - %s", id))));
        out.println("");
    }
}
