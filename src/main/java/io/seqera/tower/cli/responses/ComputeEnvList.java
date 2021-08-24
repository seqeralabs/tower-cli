package io.seqera.tower.cli.responses;

import io.seqera.tower.model.ListComputeEnvsResponseEntry;

import java.util.List;

public class ComputeEnvList extends Response {

    private List<ListComputeEnvsResponseEntry> computeEnvs;

    public ComputeEnvList(List<ListComputeEnvsResponseEntry> computeEnvs) {
        this.computeEnvs = computeEnvs;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (ListComputeEnvsResponseEntry ce : computeEnvs) {
            res.append(String.format("- [%s] (%s) %s%n", ce.getId(), ce.getPlatform(), ce.getName()));
        }
        return res.toString();
    }
}
