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


package io.seqera.tower.cli.responses.runs;

import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.List;

public class RunsDeleted extends Response {

    public final List<String> deleted;
    public final List<String> failed;
    public final String workspaceRef;

    public RunsDeleted(List<String> deleted, List<String> failed, String workspaceRef) {
        this.deleted = deleted;
        this.failed = failed;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println();
        deleted.forEach(id -> out.println(ansi(String.format("  @|yellow Pipeline run '%s' deleted at %s workspace|@", id, workspaceRef))));
        failed.forEach(id -> out.println(ansi(String.format("  @|bold,red Pipeline run '%s' could not be deleted at %s workspace|@", id, workspaceRef))));
    }

    @Override
    public int getExitCode() {
        return failed.isEmpty() ? CommandLine.ExitCode.OK : CommandLine.ExitCode.SOFTWARE;
    }
}
