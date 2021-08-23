package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GitlabProvider extends AbstractGitProvider {

    @Option(names = {"-u", "--username"}, description = "Gitlab username", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Github account password or access token (recommended)", interactive = true, required = true)
    public String password;

    @Option(names = {"-t", "--token"}, description = "Gitlab account access token", required = true, interactive = true)
    public String accessToken;

    public GitlabProvider() {
        super(ProviderEnum.GITLAB);
    }
    
    @Override
    public SecurityKeys securityKeys() throws IOException {
        return new SecurityKeys()
                .username(userName)
                .password(password)
                .token(accessToken);
    }
}
