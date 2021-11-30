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
import io.seqera.tower.cli.utils.FilesHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DatasetDownload extends Response {

    public final File file;
    public final String fileName;

    public DatasetDownload(File file, String fileName) {
        this.file = file;
        this.fileName = fileName;
    }

    @Override
    public Object getJSON() {
        Map<String, Object> data = new HashMap<>();
        data.put(fileName, FilesHelper.readFileAndDelete(file));

        return data;
    }

    @Override
    public String toString() {
        return ansi(FilesHelper.readFileAndDelete(file));
    }
}
