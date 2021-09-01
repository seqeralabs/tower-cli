package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.computeenv.CreateCmd;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import io.seqera.tower.cli.responses.ComputeEnvCreated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreateComputeEnvRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command
public abstract class AbstractCreateCmd extends AbstractApiCmd {

    @ParentCommand
    protected CreateCmd parent;

    @Option(names = {"-n", "--name"}, description = "Compute environment name", required = true)
    public String name;

    @Option(names = {"-c", "--credentials-id"}, description = "Credentials identifier", required = true)
    public String credentialsId;

    @Override
    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        return createComputeEnv(getPlatform().computeConfig());
    }

    protected ComputeEnvCreated createComputeEnv(ComputeConfig config) throws ApiException {
        api().createComputeEnv(
                new CreateComputeEnvRequest().computeEnv(
                        new ComputeEnv()
                                .name(name)
                                .platform(ComputeEnv.PlatformEnum.fromValue(config.getPlatform()))
                                .credentialsId(credentialsId)
                                .config(config)
                ), workspaceId()
        );

        return new ComputeEnvCreated(config.getPlatform(), name, workspaceRef());
    }

    protected abstract Platform getPlatform();

}
