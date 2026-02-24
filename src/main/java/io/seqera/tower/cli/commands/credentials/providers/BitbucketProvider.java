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

import io.seqera.tower.model.BitBucketSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class BitbucketProvider extends AbstractGitProvider<BitBucketSecurityKeys> {

    @Option(names = {"-u", "--username"}, description = "Bitbucket username for repository authentication.", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "Bitbucket app password or access token. App passwords are recommended for API access. Create app passwords in Bitbucket Settings > Personal settings > App passwords.", arity = "0..1", interactive = true, required = true)
    public String password;

    public BitbucketProvider() {
        super(ProviderEnum.BITBUCKET);
    }

    @Override
    public BitBucketSecurityKeys securityKeys() throws IOException {
        return new BitBucketSecurityKeys()
                .username(userName)
                .password(password);
    }
}
