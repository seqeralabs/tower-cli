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

package io.seqera.tower.cli.responses.participants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ParticipantResponseDto;
import io.seqera.tower.model.ParticipantType;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatParticipantType;

public class ParticipantsList extends Response {

    public final String organizationName;
    public final String workspaceName;
    public final List<ParticipantResponseDto> participants;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public ParticipantsList(String organizationName, String workspaceName, List<ParticipantResponseDto> participants, @Nullable PaginationInfo paginationInfo) {
        this.organizationName = organizationName;
        this.workspaceName = workspaceName;
        this.participants = participants;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Participants for '%s/%s' workspace:|@%n", organizationName, workspaceName)));

        if (participants.isEmpty()) {
            out.println(ansi("    @|yellow No participants found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Participant Type", "Name", "Workspace Role").sortBy(0);
        table.setPrefix("    ");
        participants.forEach(element -> {
            String name = element.getType() == ParticipantType.TEAM ?  element.getTeamName() : element.getUserName() + " (" + element.getEmail() + ")";
            table.addRow(element.getParticipantId().toString(), formatParticipantType(element.getType()), name, element.getWspRole().toString());
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
