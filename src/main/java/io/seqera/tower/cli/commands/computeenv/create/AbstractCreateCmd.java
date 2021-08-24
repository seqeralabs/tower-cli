package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractCmd;
import io.seqera.tower.cli.commands.computeenv.CreateCmd;
import io.seqera.tower.cli.commands.computeenv.platforms.AbstractPlatform;
import io.seqera.tower.cli.responses.ComputeEnvCreated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.CreateComputeEnvRequest;
import io.seqera.tower.model.CreateComputeEnvResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import java.io.IOException;

@Command
public abstract class AbstractCreateCmd extends AbstractCmd {

    @ParentCommand
    protected CreateCmd parent;

    @Option(names = {"-n", "--name"}, description = "Compute environment name")
    public String name;

    @Option(names = {"--credentials-id"}, description = "Credentials identifier")
    public String credentialsId;

    @Override
    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        PlatformEnum platform = getPlatform().type();
        ComputeConfig config = getPlatform().computeConfig();

        CreateComputeEnvResponse response = api().createComputeEnv(
                new CreateComputeEnvRequest().computeEnv(
                        new ComputeEnv()
                                .name(name)
                                .platform(platform)
                                .credentialsId(credentialsId)
                                .config(config)
                ), workspaceId()
        );

        return new ComputeEnvCreated(platform.name(), name, workspaceRef());
    }

    protected abstract AbstractPlatform getPlatform();

}
