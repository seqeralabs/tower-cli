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

package io.seqera.tower.cli.responses.data;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;

public class DataLinkFileDownloadResult extends Response {

    public final List<String> paths;

    public DataLinkFileDownloadResult(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Successfully downloaded files |@%n")));
        out.println("");

        List<String> descriptions = new ArrayList<>(List.of("Files downloaded"));
        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()])).sortBy(0);
        table.setPrefix("    ");

        paths.forEach(path -> {
            List<String> rows = new ArrayList<>(List.of(
                    path == null ? "" : path
            ));
            table.addRow(rows.toArray(new String[rows.size()]));
        });
        table.print();
        out.println("");
    }
}
