package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class BitbucketProvider extends AbstractGitProvider {

    @Option(names = {"-u", "--username"}, description = "Bitbucket username", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Bitbucket App password", interactive = true, required = true)
    public String password;

    public BitbucketProvider() {
        super(ProviderEnum.BITBUCKET);
    }

    @Override
    public SecurityKeys securityKeys() throws IOException {
        return new SecurityKeys()
                .username(userName)
                .password(password);
    }
}
