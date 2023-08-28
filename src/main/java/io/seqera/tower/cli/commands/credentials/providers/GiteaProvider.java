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
import io.seqera.tower.model.GiteaSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GiteaProvider extends AbstractGitProvider<GiteaSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Gitea username.", required = true)
    public String userName;

    // NOTE: setting 'arity' + 'interactive' allows both passing value as param and prompting user for input
    @Option(names = {"-p", "--password"}, description = "Gitea account password.", arity = "0..1", interactive = true, required = true)
    public String password;

    public GiteaProvider() {
        super(ProviderEnum.GITEA);
    }

    @Override
    public GiteaSecurityKeys securityKeys() throws IOException {
        return new GiteaSecurityKeys()
                .username(userName)
                .password(password);
    }
}
