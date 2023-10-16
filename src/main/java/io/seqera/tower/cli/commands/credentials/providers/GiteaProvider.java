/*
 * Copyright 2023, Seqera.
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
 *
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
