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

package io.seqera.tower.cli.responses.datasets;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Dataset;

import java.io.PrintWriter;

public class DatasetView extends Response {

    public final Dataset dataset;
    public final String workspace;

    public DatasetView(Dataset dataset, String workspace) {
        this.dataset = dataset;
        this.workspace = workspace;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Dataset at %s workspace:|@%n", workspace)));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", dataset.getId());
        table.addRow("Name", dataset.getName());
        table.addRow("Description", dataset.getDescription());
        table.addRow("Media Type", dataset.getMediaType());
        table.addRow("Created", FormatHelper.formatDate(dataset.getDateCreated()));
        table.addRow("Updated", FormatHelper.formatDate(dataset.getLastUpdated()));

        table.print();
    }
}
