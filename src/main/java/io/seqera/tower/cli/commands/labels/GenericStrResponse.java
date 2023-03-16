package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.responses.Response;

import java.io.PrintWriter;

public class GenericStrResponse extends Response {

    public String msg;

    GenericStrResponse(final String msg) {
        if (!msg.endsWith(System.lineSeparator())) {
            this.msg = msg + System.lineSeparator();
        } else {
            this.msg = msg;
        }
    }

    @Override
    public void toString(PrintWriter out) {
        out.printf(this.msg);
    }

}
