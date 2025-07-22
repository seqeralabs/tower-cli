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

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataLinkDto;
import io.seqera.tower.model.DataLinkItem;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public class DataLinkContentList extends Response {

    public final DataLinkDto dataLink;
    public final String path;
    public final List<DataLinkItem> items;
    public final String nextPageToken;

    public DataLinkContentList(DataLinkDto dataLink, String path, List<DataLinkItem> items, String nextPageToken) {
        this.dataLink = dataLink;
        this.path = path;
        this.items = items;
        this.nextPageToken = nextPageToken;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Content of '%s' and path '%s':|@%n", dataLink.getResourceRef(), path)));

        TableList table = new TableList(out, 3,  "Type", "Name", "Size").sortBy(0);

        items.forEach(item -> {
            long size = item.getSize() != null ? item.getSize() : 0L;
            table.addRow(
                    Objects.requireNonNull(item.getType()).getValue(),
                    item.getName(),
                    String.valueOf(size)
            );
        });

        table.print();

        if (nextPageToken != null)
            out.println(ansi(String.format("%n  @|bold Next page token to fetch the next page: %s|@%n", nextPageToken)));

        out.println("");
    }
}
