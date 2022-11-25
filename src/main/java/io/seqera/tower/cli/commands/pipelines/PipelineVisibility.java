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

package io.seqera.tower.cli.commands.pipelines;

public enum PipelineVisibility {
    ALL("all"),
    PRIVATE("private"),
    SHARED("shared");

    private final String value;

    PipelineVisibility(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
