/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.responses.participants;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;

public class ParticipantsList extends Response {

    public final String organizationName;
    public final String workspaceName;
    public final List<ParticipantDbDto> participants;

    public ParticipantsList(String organizationName, String workspaceName, List<ParticipantDbDto> participants) {
        this.organizationName = organizationName;
        this.workspaceName = workspaceName;
        this.participants = participants;
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
            table.addRow(element.getParticipantId().toString(), element.getType().toString(), name, element.getWspRole().toString());
        });

        table.print();
        out.println("");
    }
}
