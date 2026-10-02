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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.SshKeyNotFoundException;
import io.seqera.tower.model.UserSshPublicKeyDto;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractSshKeysCmd extends AbstractApiCmd {

    protected UserSshPublicKeyDto fetchSshKey(SshKeyRefOptions ref) throws ApiException {
        if (ref.sshKey.id != null) {
            return sshKeysApi().describeSshKey(ref.sshKey.id).getSshKey();
        }
        return sshKeysApi().listSshKeys().getSshKeys().stream()
                .filter(key -> ref.sshKey.name.equals(key.getName()))
                .findFirst()
                .orElseThrow(() -> new SshKeyNotFoundException(ref.sshKey.name));
    }
}
