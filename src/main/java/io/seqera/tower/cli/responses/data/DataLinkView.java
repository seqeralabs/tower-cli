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

import java.io.PrintWriter;
import java.util.Objects;

public class DataLinkView extends Response {

    public final DataLinkDto dataLink;

    public final String message;

    public DataLinkView(DataLinkDto dataLink, String message) {
        this.dataLink = dataLink;
        this.message = message;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold %s:|@%n", message)));

        TableList table = new TableList(out, 5, "ID", "Provider", "Name", "Resource ref", "Region").sortBy(0);

        table.addRow(
                dataLink.getId(),
                Objects.requireNonNull(dataLink.getProvider()).toString(),
                dataLink.getName(),
                dataLink.getResourceRef(),
                dataLink.getRegion()
        );

        table.print();

        out.println("");
    }

}
