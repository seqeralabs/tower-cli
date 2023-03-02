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

package io.seqera.tower.cli.responses.runs;

import io.seqera.tower.cli.responses.Response;

import java.nio.file.Path;

public class RunDump extends Response {

    public final String id;
    public final String workspaceRef;
    public final Path outputFile;

    public RunDump(String id, String workspaceRef, Path outputFile) {
        this.id = id;
        this.workspaceRef = workspaceRef;
        this.outputFile = outputFile;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline run '%s' at %s workspace details dump at '%s' |@%n", id, workspaceRef, outputFile.getFileName()));
    }
}
