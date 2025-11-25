/*
 * Copyright 2021-2023, Seqera.
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
import io.seqera.tower.model.DatasetDto;

import java.io.PrintWriter;
import java.util.List;

public class DatasetList extends Response {

    public final List<DatasetDto> datasetList;
    public final String workspace;

    public DatasetList(List<DatasetDto> datasetList, String workspace) {
        this.datasetList = datasetList;
        this.workspace = workspace;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Datasets at %s workspace:|@%n", workspace)));

        if (datasetList.isEmpty()) {
            out.println(ansi("    @|yellow No datasets found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "ID", "Name", "Created").sortBy(0);
        table.setPrefix("    ");
        datasetList.forEach(ds -> table.addRow(ds.getId(), ds.getName(), FormatHelper.formatDate(ds.getDateCreated())));
        table.print();

        out.println("");
    }
}
