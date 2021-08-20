package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.BaseCmd;
import io.seqera.tower.cli.commands.credentials.CreateCmd;
import io.seqera.tower.model.CreateCredentialsRequest;
import io.seqera.tower.model.CredentialsSpec;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "aws",
        description = "Create new AWS workspace credentials"
)
public class AwsCmd extends BaseCmd {

    @ParentCommand
    protected CreateCmd parent;

    @Option(names = {"-n", "--name"}, required = true)
    public String name;

    @ArgGroup(exclusive = false)
    public Keys keys;

    public static class Keys {

        @Option(names = {"--access-key"}, required = true)
        String accessKey;

        @Option(names = {"--secret-key"}, required = true)
        String secretKey;
    }

    @Option(names = {"--assume-role-arn"})
    String assumeRoleArn;

    @Override
    public Tower app() {
        return parent.app();
    }

    @Override
    protected Integer exec() throws ApiException, IOException {


        api().createCredentials(
                new CreateCredentialsRequest().credentials(
                        new CredentialsSpec()
                        .name(name)
                        .provider(CredentialsSpec.ProviderEnum.AWS)
                        .keys(securityKeys())
                ),
                workspaceId()
        );

        app().println(String.format("New AWS credentials '%s' added at %s workspace", name, workspaceRef()));
        return 0;
    }

    private SecurityKeys securityKeys() {
        SecurityKeys result = new SecurityKeys();
        if (keys != null) {
            result.accessKey(keys.accessKey).secretKey(keys.secretKey);
        }

        if (assumeRoleArn != null) {
            result.assumeRoleArn(assumeRoleArn);
        }

        return result;
    }
}
