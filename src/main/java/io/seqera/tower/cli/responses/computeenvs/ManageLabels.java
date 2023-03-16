package io.seqera.tower.cli.responses.computeenvs;

import io.seqera.tower.cli.responses.Response;

public class ManageLabels extends Response {

    @Override
    public String toString() {
        return ansi("done");
    }
}
