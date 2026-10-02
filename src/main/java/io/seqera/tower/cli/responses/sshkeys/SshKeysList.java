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


package io.seqera.tower.cli.responses.sshkeys;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.UserSshPublicKeyDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class SshKeysList extends Response {

    public final List<UserSshPublicKeyDto> sshKeys;

    public SshKeysList(List<UserSshPublicKeyDto> sshKeys) {
        this.sshKeys = sshKeys;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold SSH keys:|@%n")));
        if (sshKeys == null || sshKeys.isEmpty()) {
            out.println(ansi("    @|yellow No SSH keys found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Name", "Created", "Last used").sortBy(0);
        table.setPrefix("    ");
        sshKeys.forEach(key -> table.addRow(
                key.getId().toString(),
                key.getName(),
                formatDate(key.getDateCreated()),
                formatDate(key.getLastUsed())
        ));
        table.print();
        out.println("");
    }
}
