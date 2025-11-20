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

package io.seqera.tower.cli.responses.studios;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioStatusInfo;
import io.seqera.tower.model.UserInfo;

import static io.seqera.tower.cli.utils.FormatHelper.formatDescription;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;
import static io.seqera.tower.cli.utils.FormatHelper.formatStudioStatus;

public class StudiosList extends Response {

    public final String workspaceRef;
    public final List<DataStudioDto> studios;

    public boolean showLabels;
    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public StudiosList(String workspaceRef, List<DataStudioDto> studios, boolean showLabels, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.studios = studios;
        this.showLabels = showLabels;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Studios at %s workspace:|@%n", workspaceRef)));

        if (studios.isEmpty()) {
            out.println(ansi("    @|yellow No studios found|@"));
            return;
        }

        List<String> descriptions = new ArrayList<>(List.of("ID", "Name", "Description", "User", "Status"));
        if (showLabels) descriptions.add("Labels");

        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()]));
        table.setPrefix("    ");

        studios.forEach(studio -> {

            DataStudioStatusInfo statusInfo = studio.getStatusInfo();
            UserInfo user = studio.getUser();
            List<String> rows = new ArrayList<>(List.of(
                    studio.getSessionId() == null ? "NA" : studio.getSessionId(),
                    studio.getName() == null ? "NA" : studio.getName(),
                    formatDescription(studio.getDescription(), 100),
                    user == null ? "NA" : user.getUserName(),
                    formatStudioStatus(statusInfo == null ? null : statusInfo.getStatus())
            ));
            if (showLabels) rows.add(formatLabels(studio.getLabels()));

            table.addRow(rows.toArray(new String[rows.size()]));
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }

}
