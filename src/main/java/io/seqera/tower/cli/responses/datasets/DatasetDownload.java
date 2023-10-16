/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
