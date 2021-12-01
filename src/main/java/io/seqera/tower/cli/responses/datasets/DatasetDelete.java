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

public class DatasetDelete extends Response {

    public final String datasetRef;
    public final String workspaceRef;

    public DatasetDelete(String datasetRef, String workspaceRef) {
        this.datasetRef = datasetRef;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {

        return ansi(String.format("%n  @|yellow Dataset '%s' deleted at %s workspace|@%n", datasetRef, workspaceRef));
    }

}
