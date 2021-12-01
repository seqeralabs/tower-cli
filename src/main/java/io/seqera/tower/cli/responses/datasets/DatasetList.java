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

package io.seqera.tower.cli.responses.datasets;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Dataset;

import java.io.PrintWriter;
import java.util.List;

public class DatasetList extends Response {

    public final List<Dataset> datasetList;
    public final String workspace;

    public DatasetList(List<Dataset> datasetList, String workspace) {
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
