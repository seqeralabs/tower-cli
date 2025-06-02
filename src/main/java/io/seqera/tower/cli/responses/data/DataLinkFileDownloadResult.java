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
import io.seqera.tower.model.DataLinkItemType;

public class DataLinkFileDownloadResult extends Response {

    public final List<SimplePathInfo> paths;

    public DataLinkFileDownloadResult(List<SimplePathInfo> paths) {
        this.paths = paths;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Successfully downloaded files |@%n")));
        out.println("");

        List<String> descriptions = new ArrayList<>(List.of( "Type", "File count","Path"));
        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()])).sortBy(0);
        table.setPrefix("    ");

        paths.forEach(pathInfo -> {
            List<String> rows = new ArrayList<>(List.of(
                    pathInfo.type == null ? "" : pathInfo.type.toString(),
                    Integer.valueOf(pathInfo.fileCount).toString(),
                    pathInfo.path == null ? "" : pathInfo.path
            ));
            table.addRow(rows.toArray(new String[rows.size()]));
        });
        table.print();
        out.println("");
    }

    public static class SimplePathInfo extends Response {

        public DataLinkItemType type;
        public String path;
        public int fileCount;

        public SimplePathInfo() {
        }

        public SimplePathInfo(DataLinkItemType type, String path, int fileCount) {
            this.type = type;
            this.path = path;
            this.fileCount = fileCount;
        }


    }

}
