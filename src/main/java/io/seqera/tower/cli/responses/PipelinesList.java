package io.seqera.tower.cli.responses;

import io.seqera.tower.model.PipelineDbDto;

import java.util.List;

public class PipelinesList extends Response {

    private final List<PipelineDbDto> pipelines;

    public PipelinesList(List<PipelineDbDto> pipelines) {
        this.pipelines = pipelines;
    }

    @Override
    public String toString() {
        if (pipelines.isEmpty()) {
            return "No pipelines found";
        }

        StringBuilder res = new StringBuilder();
        for (PipelineDbDto pipe : pipelines) {
            res.append(String.format("- [%s] %s%s%n", pipe.getName(), pipe.getRepository(), formatDescription(pipe.getDescription())));
        }
        return res.toString();
    }

    private String formatDescription(String value) {
        if (value == null) {
            return "";
        }
        return String.format(" - %s", value);
    }
}
