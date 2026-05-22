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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DatasetDto;

import jakarta.annotation.Nullable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DatasetList extends Response {

    public final List<DatasetDto> datasetList;
    public final String workspace;
    public final boolean showLabels;
    public final boolean showHidden;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public DatasetList(List<DatasetDto> datasetList, String workspace) {
        this(datasetList, workspace, false, false, null);
    }

    public DatasetList(List<DatasetDto> datasetList, String workspace, boolean showLabels, boolean showHidden, @Nullable PaginationInfo paginationInfo) {
        this.datasetList = datasetList;
        this.workspace = workspace;
        this.showLabels = showLabels;
        this.showHidden = showHidden;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Datasets at %s workspace:|@%n", workspace)));

        if (datasetList == null || datasetList.isEmpty()) {
            out.println(ansi("    @|yellow No datasets found|@"));
            return;
        }

        List<String> desc = new ArrayList<>(List.of("ID", "Name", "Created"));
        if (showHidden) desc.add("Hidden");
        if (showLabels) desc.add("Labels");

        TableList table = new TableList(out, desc.size(), desc.toArray(new String[0])).sortBy(0);
        table.setPrefix("    ");
        datasetList.forEach(ds -> {
            List<String> row = new ArrayList<>(List.of(
                    ds.getId(),
                    ds.getName(),
                    FormatHelper.formatDate(ds.getDateCreated())
            ));
            if (showHidden) row.add(Boolean.TRUE.equals(ds.getHidden()) ? "yes" : "no");
            if (showLabels) row.add(FormatHelper.formatLabels(ds.getLabels()));
            table.addRow(row.toArray(new String[0]));
        });
        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
