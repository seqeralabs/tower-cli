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
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DatasetVersionDto;

import java.io.PrintWriter;
import java.util.List;

public class DatasetVersionsList extends Response {

    public final List<DatasetVersionDto> versions;
    public final String dataset;
    public final String workspace;

    public DatasetVersionsList(List<DatasetVersionDto> versions, String dataset, String workspace) {
        this.versions = versions;
        this.dataset = dataset;
        this.workspace = workspace;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Datasets versions for dataset %s at %s workspace:|@%n", dataset, workspace)));

        if (versions.isEmpty()) {
            out.println(ansi("    @|yellow No versions found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "Version", "Has Header", "Media Type", "File Name", "URL").sortBy(0);
        table.setPrefix("    ");
        versions.forEach(v -> table.addRow(v.getVersion().toString(), v.getHasHeader().toString(), v.getMediaType(), v.getFileName(), v.getUrl()));
        table.print();

        out.println("");
    }
}
