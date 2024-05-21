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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataLinkDto;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public class DataLinksList extends Response {

    public final String workspaceRef;
    public final List<DataLinkDto> dataLinks;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public DataLinksList(String workspaceRef, List<DataLinkDto> dataLinks, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.dataLinks = dataLinks;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Data links at %s workspace:|@%n", workspaceRef)));

        if (dataLinks.isEmpty()) {
            out.println(ansi("    @|yellow No data links found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Provider", "Name", "Resource ref", "Region").sortBy(0);

        dataLinks.forEach( dl -> table.addRow(
                dl.getId(),
                Objects.requireNonNull(dl.getProvider()).toString(),
                dl.getName(),
                dl.getResourceRef(),
                dl.getRegion()
        ));

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
