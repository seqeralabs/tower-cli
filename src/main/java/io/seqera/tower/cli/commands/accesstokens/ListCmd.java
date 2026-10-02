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


package io.seqera.tower.cli.commands.accesstokens;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.accesstokens.AccessTokensList;
import io.seqera.tower.model.AccessToken;
import picocli.CommandLine.Command;

import java.util.List;

@Command(
        name = "list",
        description = "List your personal access tokens"
)
public class ListCmd extends AbstractApiCmd {

    @Override
    protected Response exec() throws ApiException {
        List<AccessToken> tokens = tokensApi().tokenList().getTokens().stream()
                // Drop the deprecated basicAuth field: it is a usable credential for legacy tokens
                .map(token -> new AccessToken()
                        .id(token.getId())
                        .name(token.getName())
                        .dateCreated(token.getDateCreated())
                        .lastUsed(token.getLastUsed()))
                .toList();
        return new AccessTokensList(tokens);
    }
}
