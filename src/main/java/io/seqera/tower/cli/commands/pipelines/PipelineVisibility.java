package io.seqera.tower.cli.commands.pipelines;

public enum PipelineVisibility {
    ALL("all"),
    PRIVATE("private"),
    SHARED("shared");

    private final String value;

    PipelineVisibility(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
