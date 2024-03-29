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

package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.Map;

public class InfoResponse extends Response {

    final public int connectionCheck;
    final public int versionCheck;
    final public int credentialsCheck;

    final public Map<String, String> opts;

    public InfoResponse(int connectionCheck, int versionCheck, int credentialsCheck, Map<String, String> opts) {
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
        String skipped = ansi("@|fg(yellow) SKIPPED|@");

        out.println(ansi(String.format("%n    @|bold,fg(yellow) Details|@")));
        TableList detailsTable = new TableList(out, 2);
        detailsTable.setPrefix("    ");
        detailsTable.addRow("Tower API endpoint", getOpt("towerApiEndpoint", undefined));
        detailsTable.addRow("Tower API version", getOpt("towerApiVersion", undefined));
        detailsTable.addRow("Tower version", getOpt("towerVersion", undefined));
        detailsTable.addRow("CLI version", getOpt("cliVersion", undefined));
        detailsTable.addRow("CLI minimum API version", getOpt("cliApiVersion", undefined));
        detailsTable.addRow("Authenticated user", getOpt("userName", undefined));
        detailsTable.print();

        out.println(ansi(String.format("%n    @|bold,fg(yellow) System health status|@")));
        TableList healthTable = new TableList(out, 2);
        healthTable.setPrefix("    ");
        healthTable.addRow("Remote API server connection check", connectionCheck == 1 ? ok : connectionCheck == 0 ? fail : skipped);
        healthTable.addRow("Tower API version check", versionCheck == 1 ? ok : versionCheck == 0 ? fail : skipped);
        healthTable.addRow("Authentication API credential's token", credentialsCheck == 1 ? ok : credentialsCheck == 0 ? fail : skipped);
        healthTable.print();

        if (connectionCheck == 0) {
            if (opts.get("towerApiEndpoint").contains("/api")) {
                out.println(ansi(String.format("%n    @|bold,fg(red) Tower API URL %s is not available|@%n", opts.get("towerApiEndpoint"))));
            } else {
                out.println(ansi(String.format("%n    @|bold,fg(red) Tower API URL %s is not available (did you mean %s/api?)|@%n", opts.get("towerApiEndpoint"), opts.get("towerApiEndpoint"))));
            }
        }

        if (versionCheck == 0) {
            out.println(ansi(String.format("%n    @|bold,fg(red) Tower API version is %s while the minimum required version to be fully compatible is %s|@%n", opts.get("towerApiVersion"), opts.get("cliApiVersion"))));
        }

        if (credentialsCheck == 0) {
            out.println(ansi(String.format("%n    @|bold,fg(red) Review that your current access token is valid and active.|@%n")));
        }

        out.println("");
    }

   private String getOpt(String key, String defaultValue){
        String res = opts.get(key);
        if (res != null) {
            return res;
        }
        return defaultValue;
    }

    @Override
    public int getExitCode() {
        return (connectionCheck + versionCheck + credentialsCheck == 3) ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
