package io.seqera.tower.cli.commands.computeenvs;

import java.io.IOException;
import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ManageLabels;
import picocli.CommandLine;

@CommandLine.Command(name = "labels", description = "Manages labels in compute environment.")
public class LabelsCmd extends AbstractApiCmd {


    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Parameters(split = ",", description = "")
    private List<String> labels;
    @CommandLine.Option(names = "--no-create", description = "")
    private boolean noCreate;
    @CommandLine.Option(names = {"--operations", "-o"}, description = "", defaultValue = "set")
    private Operation operation;


    @Override
    protected Response exec() throws ApiException, IOException {
        System.out.printf("%s, %s, %s, %s, %s, %s%n", workspace.workspace, computeEnvRefOptions.computeEnv.computeEnvId, computeEnvRefOptions.computeEnv.computeEnvName,
                String.join("", labels), noCreate, operation);
        return new ManageLabels();
    }

    enum Operation {
        SET, APPEND, DELETE
    }
}
