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

package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.credentials.providers.AwsProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.AwsSecurityKeys;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;

@Command(
        name = "aws",
        description = "Update AWS credentials"
)
public class UpdateAwsCmd extends AbstractUpdateCmd {

    @Mixin
    protected AwsProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }

    @Override
    protected Response update(Credentials creds, Long wspId) throws ApiException, IOException {
        // AWS credential mode cannot change after creation: default to the stored one
        if (!provider.hasMode()) {
            SecurityKeys keys = storedKeys(creds, wspId);
            provider.inheritMode(keys instanceof AwsSecurityKeys aws ? aws.getMode() : null);
        }
        return super.update(creds, wspId);
    }
}
