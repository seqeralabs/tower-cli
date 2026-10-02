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


package io.seqera.tower.cli.commands.managedidentities.credentials;

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.SSHSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class SshCredentialsOptions {

    @Option(names = {"-l", "--linux-username"}, description = "Linux username of the organization member on the cluster.", required = true)
    public String linuxUserName;

    @Option(names = {"-k", "--key"}, description = "Path to the SSH private key file used to connect to the cluster.", required = true)
    public Path privateKey;

    @Option(names = {"-p", "--passphrase"}, description = "Passphrase for an encrypted SSH private key.", arity = "0..1", interactive = true)
    public String passphrase;

    public SSHSecurityKeys securityKeys() throws IOException {
        return new SSHSecurityKeys()
                .privateKey(FilesHelper.readString(privateKey))
                .passphrase(passphrase);
    }
}
