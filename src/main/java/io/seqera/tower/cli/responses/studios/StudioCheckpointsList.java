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
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataStudioCheckpointDto;
import io.seqera.tower.model.StudioUser;

import static io.seqera.tower.cli.utils.FormatHelper.formatTime;

public class StudioCheckpointsList extends Response {

    public final String userSuppliedStudioIdentifier;
    public final String workspaceRef;
    public final List<DataStudioCheckpointDto> checkpoints;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public StudioCheckpointsList(String userSuppliedStudioIdentifier, String workspaceRef, List<DataStudioCheckpointDto> checkpoints, @Nullable PaginationInfo paginationInfo) {
        this.userSuppliedStudioIdentifier = userSuppliedStudioIdentifier;
        this.workspaceRef = workspaceRef;
        this.checkpoints = checkpoints;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Checkpoints at Studio %s at %s workspace:|@%n", userSuppliedStudioIdentifier, workspaceRef)));

        if (checkpoints.isEmpty()) {
            out.println(ansi("    @|yellow No checkpoints found|@"));
            return;
        }

        List<String> descriptions = new ArrayList<>(List.of("ID", "Name", "Author", "Date Created", "Date Saved"));

        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()]));
        table.setPrefix("    ");

        checkpoints.forEach(checkpoint -> {
            StudioUser user = checkpoint.getAuthor();

            List<String> rows = new ArrayList<>(List.of(
                    checkpoint.getId() == null ? "" : checkpoint.getId().toString(),
                    checkpoint.getName() == null ? "" : checkpoint.getName(),
                    user == null ? "" : user.getUserName(),
                    formatTime(checkpoint.getDateCreated()),
                    formatTime(checkpoint.getDateSaved())
            ));
            table.addRow(rows.toArray(new String[rows.size()]));
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }

}
