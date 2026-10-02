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
import io.seqera.tower.cli.exceptions.AccessTokenNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.accesstokens.AccessTokenDeleted;
import io.seqera.tower.model.AccessToken;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "delete",
        description = "Delete a personal access token. Clients using it can no longer authenticate."
)
public class DeleteCmd extends AbstractApiCmd {

    @ArgGroup(multiplicity = "1")
    public TokenRef ref;

    public static class TokenRef {

        @Option(names = {"-i", "--id"}, description = "Token numeric identifier")
        public Long id;

        @Option(names = {"-n", "--name"}, description = "Token name")
        public String name;
    }

    @Override
    protected Response exec() throws ApiException {
        AccessToken token = tokensApi().tokenList().getTokens().stream()
                .filter(it -> ref.id != null ? ref.id.equals(it.getId()) : ref.name.equals(it.getName()))
                .findFirst()
                .orElseThrow(() -> new AccessTokenNotFoundException(ref.id != null ? ref.id.toString() : ref.name));
        tokensApi().deleteToken(token.getId());
        return new AccessTokenDeleted(token.getId(), token.getName());
    }
}
