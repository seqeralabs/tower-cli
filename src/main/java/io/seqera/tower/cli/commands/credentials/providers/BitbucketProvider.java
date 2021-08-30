package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.BitBucketSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class BitbucketProvider extends AbstractGitProvider<BitBucketSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Bitbucket username", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Bitbucket App password", interactive = true, required = true)
    public String password;

    public BitbucketProvider() {
        super(ProviderEnum.BITBUCKET);
    }

    @Override
    public BitBucketSecurityKeys securityKeys() throws IOException {
        return new BitBucketSecurityKeys()
                .provider(ProviderEnum.BITBUCKET.getValue())
                .username(userName)
                .password(password);
    }
}
