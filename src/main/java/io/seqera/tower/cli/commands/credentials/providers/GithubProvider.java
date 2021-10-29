/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.GitHubSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GithubProvider extends AbstractGitProvider<GitHubSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Github username", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Github account password or access token (recommended)", arity = "0..1", interactive = true, required = true)
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
