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

package io.seqera.tower.cli.responses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RunFileDownloaded extends Response {

    public final File file;

    public RunFileDownloaded(File file) {
        this.file = file;
    }

    @Override
    public Object getJSON() {
        Map<String, Object> data = new HashMap<>();
        data.put("log", readFile());

        return data;
    }

    @Override
    public String toString() {
        return ansi(readFile());
    }

    private String readFile() {
        String outcome = null;

        try {
            outcome = Files.readString(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outcome;
    }
}
