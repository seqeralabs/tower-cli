package io.seqera.tower.cli.commands.computeenvs.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.computeenvs.CreateCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.ComputeEnvCreated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreateComputeEnvRequest;
import io.seqera.tower.model.Credentials;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.List;

@Command
public abstract class AbstractCreateCmd extends AbstractApiCmd {

    @Option(names = {"-n", "--name"}, description = "Compute environment name", required = true)
    public String name;
    @Option(names = {"-c", "--credentials-id"}, description = "Credentials identifier (defaults to use the workspace credentials)")
    public String credentialsId;
    @ParentCommand
    protected CreateCmd parent;




    @Override
    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        return createComputeEnv(getPlatform().type(), getPlatform().computeConfig());
    }

    protected ComputeEnvCreated createComputeEnv(ComputeEnv.PlatformEnum platform, ComputeConfig config) throws ApiException {

        String credsId = credentialsId == null ? findWorkspaceCredentials(platform) : credentialsId;

        api().createComputeEnv(
                new CreateComputeEnvRequest().computeEnv(
                        new ComputeEnv()
                                .name(name)
                                .platform(platform)
                                .credentialsId(credsId)
                                .config(config)
                ), workspaceId()
        );

        return new ComputeEnvCreated(platform.getValue(), name, workspaceRef());
    }

    private String findWorkspaceCredentials(ComputeEnv.PlatformEnum type) throws ApiException {
        List<Credentials> credentials = api().listCredentials(workspaceId(), type.getValue()).getCredentials();
        if (credentials.isEmpty()) {
            throw new TowerException("No valid credentials found at the workspace");
        }

        if (credentials.size() > 1) {
            throw new TowerException("Multiple credentials match this compute environment. Please provide the credentials identifier that you want to use");
        }

        return credentials.get(0).getId();
    }

    protected abstract Platform getPlatform();

}
