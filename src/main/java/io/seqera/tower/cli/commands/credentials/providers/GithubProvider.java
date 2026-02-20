/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.GitHubSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GithubProvider extends AbstractGitProvider<GitHubSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "GitHub username for repository authentication.", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "GitHub password or personal access token. Use of personal access tokens is recommended for security. Generate tokens at Settings > Developer settings > Personal access tokens.", arity = "0..1", interactive = true, required = true)
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
