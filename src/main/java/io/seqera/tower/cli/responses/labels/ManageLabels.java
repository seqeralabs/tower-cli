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

package io.seqera.tower.cli.responses.labels;

import io.seqera.tower.cli.responses.Response;

public class ManageLabels extends Response {

    public String operation;
    public String type;

    public final String id;

    public final Long workspaceID;


    public ManageLabels(String operation, String type, String id, Long workspaceID) {
        this.operation = operation;
        this.type = type;
        this.id = id;
        this.workspaceID = workspaceID;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n @|yellow '%s' labels on '%s' with id '%s' at %s workspace|@%n",
                operation, type, id, workspaceID));
    }
}
