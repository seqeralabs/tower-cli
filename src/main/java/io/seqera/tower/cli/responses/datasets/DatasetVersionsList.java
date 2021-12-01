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
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DatasetVersionDbDto;

import java.io.PrintWriter;
import java.util.List;

public class DatasetVersionsList extends Response {

    public final List<DatasetVersionDbDto> versions;
    public final String dataset;
    public final String workspace;

    public DatasetVersionsList(List<DatasetVersionDbDto> versions, String dataset, String workspace) {
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
