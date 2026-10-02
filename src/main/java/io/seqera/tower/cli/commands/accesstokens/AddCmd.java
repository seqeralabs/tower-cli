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
import io.seqera.tower.cli.responses.accesstokens.AccessTokenAdded;
import io.seqera.tower.model.CreateAccessTokenRequest;
import io.seqera.tower.model.CreateAccessTokenResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "add",
        description = "Add a personal access token. The token value is shown only once."
)
public class AddCmd extends AbstractApiCmd {

    @Option(names = {"-n", "--name"}, description = "Token name. A label to remember what the token is for. Must be unique.", required = true)
    public String name;

    @Override
    protected Response exec() throws ApiException {
        CreateAccessTokenResponse response = tokensApi().createToken(new CreateAccessTokenRequest().name(name));
        return new AccessTokenAdded(response.getToken().getId(), response.getToken().getName(), response.getAccessKey());
    }
}
