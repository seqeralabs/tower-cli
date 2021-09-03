package io.seqera.tower.cli.responses;

import picocli.CommandLine;

import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Response {

    public Object getBody() {
        return this;
    }

    public void toString(PrintWriter out) {
        out.println(this);
    }

    protected String ansi(String value) {
        return CommandLine.Help.Ansi.AUTO.string(value);
    }

    protected String formatTime(OffsetDateTime value) {
        if (value == null) {
            return "never";
        }

        return value.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

}
