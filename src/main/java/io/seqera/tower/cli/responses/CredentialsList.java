package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Credentials;

import java.io.PrintWriter;
import java.util.List;

public class CredentialsList extends Response {

    private String workspaceRef;
    private final List<Credentials> credentials;

    public CredentialsList(String workspaceRef, List<Credentials> credentials) {
        this.workspaceRef = workspaceRef;
        this.credentials = credentials;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Credentials at %s workspace:|@%n", workspaceRef)));

        if (credentials.isEmpty()) {
            out.println(ansi("    @|yellow No credentials found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Provider", "Name", "Last activity").sortBy(0).withUnicode(false);
        table.setPrefix("    ");
        credentials.forEach(element -> table.addRow(element.getId(), element.getProvider().getValue(), element.getName(), formatTime(element.getLastUsed())));

        table.print();
        out.println("");
    }
}
