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

package io.seqera.tower.cli.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Response {

    @JsonIgnore
    public Object getJSON() {
        return this;
    }

    public void toString(PrintWriter out) {
        out.println(this);
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        toString(new PrintWriter(writer));
        return writer.toString();
    }

    protected String ansi(String value) {
        return CommandLine.Help.Ansi.AUTO.string(value);
    }

    @JsonIgnore
    public int getExitCode() {
        return CommandLine.ExitCode.OK;
    }

}
