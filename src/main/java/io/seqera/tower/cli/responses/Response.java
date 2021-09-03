package io.seqera.tower.cli.responses;

import picocli.CommandLine;

import java.io.PrintWriter;

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

}
