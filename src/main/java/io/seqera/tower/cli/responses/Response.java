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

import com.fasterxml.jackson.annotation.JsonIgnore;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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

    public int getExitCode() {
        return CommandLine.ExitCode.OK;
    }

}
