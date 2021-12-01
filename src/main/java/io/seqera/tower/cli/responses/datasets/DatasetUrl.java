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

import java.io.PrintWriter;

public class DatasetUrl extends Response {

    public final String datasetUrl;
    public final String dataset;
    public final String workspace;

    public DatasetUrl(String datasetUrl, String dataset, String workspace) {
        this.datasetUrl = datasetUrl;
        this.dataset = dataset;
        this.workspace = workspace;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Dataset URL|@")));
        out.println(ansi(String.format("%n  @|bold -----------|@")));
        out.println(ansi(String.format("%n  %s", datasetUrl)));
    }
}
