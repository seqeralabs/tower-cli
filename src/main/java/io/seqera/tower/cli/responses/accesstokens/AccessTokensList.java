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


package io.seqera.tower.cli.responses.accesstokens;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AccessToken;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class AccessTokensList extends Response {

    public final List<AccessToken> tokens;

    public AccessTokensList(List<AccessToken> tokens) {
        this.tokens = tokens;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Personal access tokens:|@%n")));
        if (tokens == null || tokens.isEmpty()) {
            out.println(ansi("    @|yellow No access tokens found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Name", "Created", "Last used").sortBy(0);
        table.setPrefix("    ");
        tokens.forEach(token -> table.addRow(
                token.getId().toString(),
                token.getName(),
                formatDate(token.getDateCreated()),
                formatDate(token.getLastUsed())
        ));
        table.print();
        out.println("");
    }
}
