package io.seqera.tower.cli.exceptions;

public class MultiplePipelinesFoundException extends TowerException {

    public MultiplePipelinesFoundException(String word, String workspaceRef) {
        super(String.format("Multiple pipelines match '%s' at %s workspace", word, workspaceRef));
    }
}
