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

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.SSHSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class SshProvider extends AbstractProvider<SSHSecurityKeys> {

    @Option(names = {"-k", "--key"}, description = "SSH private key file.", required = true)
    public Path serviceAccountKey;

    @Option(names = {"-p", "--passphrase"}, description = "Passphrase associated with the private key.", arity = "0..1", interactive = true)
    public String passphrase;

    public SshProvider() {
        super(ProviderEnum.SSH);
    }

    @Override
    public SSHSecurityKeys securityKeys() throws IOException {
        return new SSHSecurityKeys()
                .privateKey(FilesHelper.readString(serviceAccountKey))
                .passphrase(passphrase);
    }
}
