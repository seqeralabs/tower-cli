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

    protected String formatTime(OffsetDateTime value) {
        if (value == null) {
            return "never";
        }

        return value.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public int getExitCode() {
        return 0;
    }

}
