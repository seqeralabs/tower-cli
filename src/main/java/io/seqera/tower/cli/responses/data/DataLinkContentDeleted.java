/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.responses.data;

import io.seqera.tower.cli.responses.Response;

import java.util.List;

public class DataLinkContentDeleted extends Response {

    public final String id;
    public final Long wspId;
    public final List<String> files;
    public final List<String> dirs;

    public DataLinkContentDeleted(String id, Long wspId, List<String> files, List<String> dirs) {
        this.id = id;
        this.wspId = wspId;
        this.files = files;
        this.dirs = dirs;
    }

    @Override
    public String toString() {
        int count = (files == null ? 0 : files.size()) + (dirs == null ? 0 : dirs.size());
        if (wspId != null) {
            return ansi(String.format("%n  @|yellow %d items deleted from data link '%s' at '%s' workspace.|@%n", count, id, wspId));
        }
        return ansi(String.format("%n  @|yellow %d items deleted from data link '%s' at user workspace.|@%n", count, id));
    }
}
