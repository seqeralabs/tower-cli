package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.ComputeEnvs.ComputeEnvExport;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreateComputeEnvRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export compute environment for further creation"
)
public class ExportCmd extends AbstractComputeEnvCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Compute environment name", required = true)
    public String name;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to export", arity = "0..1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        ComputeEnv ce = findComputeEnvironmentByName(name, workspaceId());

        ComputeEnv computeEnv = new ComputeEnv();
        computeEnv.setDescription(ce.getDescription());
        computeEnv.setCredentialsId(ce.getCredentialsId());
        computeEnv.setMessage(ce.getMessage());
        computeEnv.setPlatform(ce.getPlatform());
        computeEnv.setConfig(ce.getConfig());

        CreateComputeEnvRequest request = new CreateComputeEnvRequest();
        request.setComputeEnv(computeEnv);

        return new ComputeEnvExport(request, fileName);
    }
}
