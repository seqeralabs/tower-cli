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

import io.seqera.tower.model.CodeCommitSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

public class CodeCommitProvider extends AbstractGitProvider<CodeCommitSecurityKeys> {

    @Option(names = {"--access-key"}, description = "AWS access key identifier for CodeCommit authentication. Part of AWS IAM user credentials with CodeCommit permissions.", required = true)
    public String accessKey;

    @Option(names = {"--secret-key"}, description = "AWS secret access key for CodeCommit authentication. Part of AWS IAM user credentials with CodeCommit permissions. Keep this value secure.", required = true)
    public String secretKey;

    public CodeCommitProvider() {
        super(ProviderEnum.CODECOMMIT);
    }

    @Override
    public CodeCommitSecurityKeys securityKeys() {
        return new CodeCommitSecurityKeys()
                .username(accessKey)
                .password(secretKey);
    }

}
