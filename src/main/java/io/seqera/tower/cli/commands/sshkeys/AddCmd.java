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


package io.seqera.tower.cli.commands.sshkeys;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.sshkeys.SshKeyAdded;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.CreateSshKeyRequest;
import io.seqera.tower.model.UserSshPublicKeyDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

@Command(
        name = "add",
        description = "Add an SSH public key"
)
public class AddCmd extends AbstractSshKeysCmd {

    @Option(names = {"-n", "--name"}, description = "SSH key name. Must be unique. Names consist of alphanumeric, hyphen, and underscore characters.", required = true)
    public String name;

    @Option(names = {"-k", "--key"}, description = "Path to the SSH public key file (e.g. ~/.ssh/id_ed25519.pub). Use '-' to read it from stdin. Create a key pair with 'ssh-keygen'.", required = true)
    public Path publicKey;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreateSshKeyRequest request = new CreateSshKeyRequest()
                .name(name)
                .publicKey(FilesHelper.readStringOrStdin(publicKey).strip());
        UserSshPublicKeyDto sshKey = sshKeysApi().createSshKey(request).getSshKey();
        return new SshKeyAdded(sshKey.getId(), sshKey.getName());
    }
}
