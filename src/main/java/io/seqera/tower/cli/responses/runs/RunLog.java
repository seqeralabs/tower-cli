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

import java.io.PrintWriter;
import java.util.List;

public class RunLog extends Response {

    public final List<String> entries;
    public final boolean truncated;
    public final String message;

    public RunLog(List<String> entries, boolean truncated, String message) {
        this.entries = entries;
        this.truncated = truncated;
        this.message = message;
    }

    @Override
    public void toString(PrintWriter out) {
        if (entries != null) {
            entries.forEach(out::println);
        }
        if (message != null) {
            out.println(ansi(String.format("%n  @|yellow %s|@", message)));
        }
        if (truncated) {
            out.println(ansi(String.format("%n  @|yellow Log truncated. Use 'tw runs view download' to get the complete file once the run has finished.|@")));
        }
    }
}
