/*
 * Copyright 2021-2023, Seqera.
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
import io.seqera.tower.model.AzureReposSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzureReposProvider extends AbstractGitProvider<AzureReposSecurityKeys> {

    @Option(names = { "-u", "--username" }, description = "Azure Repos username.", required = true)
    public String userName;

    @Option(names = { "-p",
            "--password" }, description = "Azure Repos account password or access token (recommended).", arity = "0..1", interactive = true, required = true)
    public String password;

    @Option(names = { "-t",
            "--token" }, description = "Azure Repos account access token.", arity = "0..1", required = true, interactive = true)
    public String accessToken;

    public AzureReposProvider() {
        super(ProviderEnum.AZUREREPOS);
    }

    @Override
    public AzureReposSecurityKeys securityKeys() throws IOException {
        return new AzureReposSecurityKeys()
                .username(userName)
                .token(accessToken);
    }
}
