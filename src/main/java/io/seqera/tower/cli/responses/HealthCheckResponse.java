package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;

public class HealthCheckResponse extends Response{

    final public int connectionCheck;
    final public int versionCheck;
    final public int credentialsCheck;

    public HealthCheckResponse(int connectionCheck, int versionCheck, int credentialsCheck) {
        this.connectionCheck = connectionCheck;
        this.versionCheck = versionCheck;
        this.credentialsCheck = credentialsCheck;
    }

    @Override
    public void toString(PrintWriter out) {
        String ok = ansi("@|fg(green) OK|@");
        String fail = ansi("@|fg(red) FAILED|@");
        String undefined = ansi("@|fg(yellow) UNDEFINED|@");

        out.println(ansi("    @|bold,fg(yellow) System health status|@"));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("Remote API server connection check", connectionCheck == 1 ? ok: connectionCheck == 0 ? fail: undefined);
        table.addRow("Tower API version check", versionCheck == 1 ? ok: versionCheck == 0 ? fail: undefined);
        table.addRow("Authentication API credential's token", credentialsCheck == 1 ? ok: credentialsCheck == 0 ? fail: undefined);
        table.print();
    }
}
