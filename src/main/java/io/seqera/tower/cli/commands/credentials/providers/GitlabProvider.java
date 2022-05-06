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

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.GitLabSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GitlabProvider extends AbstractGitProvider<GitLabSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Gitlab username.", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Gitlab account password or access token (recommended).", arity = "0..1", interactive = true, required = true)
    public String password;

    @Option(names = {"-t", "--token"}, description = "Gitlab account access token.", arity = "0..1", required = true, interactive = true)
    public String accessToken;

    public GitlabProvider() {
        super(ProviderEnum.GITLAB);
    }

    @Override
    public GitLabSecurityKeys securityKeys() throws IOException {
        return new GitLabSecurityKeys()
                .username(userName)
                .password(password)
                .token(accessToken);
    }
}
