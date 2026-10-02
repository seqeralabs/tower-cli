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

package io.seqera.tower.cli.responses.studios;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.LogPage;

import java.io.PrintWriter;
import java.util.List;

public class StudioLog extends Response {

    public final String userSuppliedStudioIdentifier;
    public final String workspaceRef;
    public final LogPage log;

    public StudioLog(String userSuppliedStudioIdentifier, String workspaceRef, LogPage log) {
        this.userSuppliedStudioIdentifier = userSuppliedStudioIdentifier;
        this.workspaceRef = workspaceRef;
        this.log = log;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Log of studio %s at %s workspace:|@%n", userSuppliedStudioIdentifier, workspaceRef)));

        // SDK 1.200 types LogPage.entries as Object (the spec says Iterator), but Platform sends a JSON array
        List<?> entries = log != null && log.getEntries() instanceof List<?> list ? list : List.of();
        if (entries.isEmpty()) {
            if (log != null && Boolean.TRUE.equals(log.getPending())) {
                out.println(ansi("    @|yellow Waiting for the output log|@"));
            } else {
                out.println(ansi("    @|yellow No log entries found|@"));
            }
            return;
        }

        entries.forEach(out::println);

        if (Boolean.TRUE.equals(log.getTruncated())) {
            out.println(ansi(String.format("%n    @|yellow Log truncated|@")));
        }
        if (log.getForwardToken() != null) {
            out.println(ansi(String.format("%n    @|bold Next page:|@ --next %s", log.getForwardToken())));
        }
        out.println("");
    }
}
