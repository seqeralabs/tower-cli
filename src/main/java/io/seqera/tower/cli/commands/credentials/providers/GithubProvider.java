package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.GitHubSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GithubProvider extends AbstractGitProvider<GitHubSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Github username", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Github account password or access token (recommended)", interactive = true, required = true)
    public String password;

    public GithubProvider() {
        super(ProviderEnum.GITHUB);
    }

    @Override
    public GitHubSecurityKeys securityKeys() throws IOException {
        return new GitHubSecurityKeys()
                .username(userName)
                .password(password);
    }
}
