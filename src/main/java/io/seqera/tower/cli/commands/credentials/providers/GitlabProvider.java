package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.GitLabSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GitlabProvider extends AbstractGitProvider<GitLabSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Gitlab username", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Gitlab account password or access token (recommended)", arity = "0..1", interactive = true, required = true)
    public String password;

    @Option(names = {"-t", "--token"}, description = "Gitlab account access token", arity = "0..1", required = true, interactive = true)
    public String accessToken;

    public GitlabProvider() {
        super(ProviderEnum.GITLAB);
    }

    @Override
    public GitLabSecurityKeys securityKeys() throws IOException {
        return new GitLabSecurityKeys()
                .provider(ProviderEnum.GITLAB.getValue())
                .username(userName)
                .password(password)
                .token(accessToken);
    }
}
