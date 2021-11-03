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

package io.seqera.tower.cli.responses.pipelines;

import io.seqera.tower.cli.responses.Response;

public class PipelinesExport extends Response {

    public final String configOutput;
    public final String fileName;

    public PipelinesExport(String configOutput, String fileName) {
        this.configOutput = configOutput;
        this.fileName = fileName;
    }

    @Override
    public String toString() {

        if (fileName != null && !fileName.equals("-")) {
            return ansi(String.format("%n  @|yellow Pipeline exported into '%s' |@%n", fileName));
        }

        return configOutput;
    }
}
