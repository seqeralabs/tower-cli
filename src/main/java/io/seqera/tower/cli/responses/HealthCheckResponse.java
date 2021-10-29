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

package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;
import java.util.Map;

public class HealthCheckResponse extends Response {

    final public int connectionCheck;
    final public int versionCheck;
    final public int credentialsCheck;

    final public Map<String, String> opts;

    public HealthCheckResponse(int connectionCheck, int versionCheck, int credentialsCheck, Map<String, String> opts) {
        this.connectionCheck = connectionCheck;
        this.versionCheck = versionCheck;
        this.credentialsCheck = credentialsCheck;
        this.opts = opts;
    }

    @Override
    public void toString(PrintWriter out) {
        String ok = ansi("@|fg(green) OK|@");
        String fail = ansi("@|fg(red) FAILED|@");
        String undefined = ansi("@|fg(yellow) UNDEFINED|@");

        out.println(ansi(String.format("%n    @|bold,fg(yellow) System health status|@")));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("Remote API server connection check", connectionCheck == 1 ? ok : connectionCheck == 0 ? fail : undefined);
        table.addRow("Tower API version check", versionCheck == 1 ? ok : versionCheck == 0 ? fail : undefined);
        table.addRow("Authentication API credential's token", credentialsCheck == 1 ? ok : credentialsCheck == 0 ? fail : undefined);
        table.print();

        if (connectionCheck == 0) {
            if (opts.get("serverUrl").contains("/api")) {
                out.println(ansi(String.format("%n    @|bold,fg(red) Tower API URL %s it is not available|@%n", opts.get("serverUrl"))));
            } else {
                out.println(ansi(String.format("%n    @|bold,fg(red) Tower API URL %s it is not available (did you mean %s/api?)|@%n", opts.get("serverUrl"), opts.get("serverUrl"))));
            }
        }

        if (versionCheck == 0) {
            out.println(ansi(String.format("%n    @|bold,fg(red) Tower API version is %s while the minimum required version to be fully compatible is %s|@%n", opts.get("systemApiVersion"), opts.get("requiredApiVersion"))));
        }

        if (credentialsCheck == 0) {
            out.println(ansi(String.format("%n    @|bold,fg(red) Review that your current access token is valid and active.|@%n")));
        }

        out.println("");
    }

    @Override
    public int getExitCode() {
        return (connectionCheck + versionCheck + credentialsCheck == 3) ? 0 : -1;
    }
}
