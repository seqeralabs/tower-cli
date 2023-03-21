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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.responses.Response;

import javax.annotation.Nullable;

public class DeleteLabelsResponse extends Response {

    Long labelId;
    @Nullable
    Long workspaceId;

    public DeleteLabelsResponse(final Long labelId, final Long workspaceId) {
        this.labelId = labelId;
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return ansi(String.format(
                "%n  @|yellow Label '%d' deleted"
                + ( workspaceId != null ? " at '%d' workspace" : "" )
                + " |@%n",
        labelId, workspaceId));
    }

}
