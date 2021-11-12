package io.seqera.tower.cli.responses;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.model.DescribeTaskResponse;

public class RunTasksView extends Response {

    public final List<DescribeTaskResponse> tasks;

    public RunTasksView(List<DescribeTaskResponse> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void toString(PrintWriter out) {
        super.toString(out);
    }
}
