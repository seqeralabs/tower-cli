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

public class DatasetCreate extends Response {

    public final String datasetName;
    public final String workspaceRef;
    public final String datasetId;

    public DatasetCreate(String datasetName, String workspaceRef, String datasetId) {
        this.datasetName = datasetName;
        this.workspaceRef = workspaceRef;
        this.datasetId = datasetId;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Dataset '%s' added at %s workspace with id '%s'|@%n", datasetName, workspaceRef, datasetId));
    }

}
