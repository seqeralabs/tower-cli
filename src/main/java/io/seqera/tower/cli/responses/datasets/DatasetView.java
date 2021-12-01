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
