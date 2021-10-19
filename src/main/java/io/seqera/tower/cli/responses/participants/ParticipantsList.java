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

        TableList table = new TableList(out, 3, "Participant Type", "Name", "Workspace Role").sortBy(0);
        table.setPrefix("    ");
        participants.forEach(element -> {
            if (element.getType() == ParticipantType.TEAM) {
                table.addRow(element.getType().toString(), element.getTeamName(), element.getWspRole().toString());
            } else {
                table.addRow(element.getType().toString(), element.getUserName() + " (" + element.getEmail() + ")", element.getWspRole().toString());
            }
        });

        table.print();
        out.println("");
    }
}
