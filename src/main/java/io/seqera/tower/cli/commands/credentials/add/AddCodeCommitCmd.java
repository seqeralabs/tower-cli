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

package io.seqera.tower.cli.commands.credentials.add;

import io.seqera.tower.cli.commands.credentials.providers.CodeCommitProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.model.CodeCommitSecurityKeys;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Command;

@Command(
        name = "codecommit",
        description = "Add new CodeCommit workspace credentials."
)
public class AddCodeCommitCmd extends AbstractAddCmd<CodeCommitSecurityKeys> {

    @Mixin
    CodeCommitProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
