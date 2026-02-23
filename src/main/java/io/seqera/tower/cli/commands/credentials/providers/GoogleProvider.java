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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.GoogleSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class GoogleProvider extends AbstractProvider<GoogleSecurityKeys> {

    @Option(names = {"-k", "--key"}, description = "Path to JSON file containing Google Cloud service account key. Download from Google Cloud Console IAM & Admin > Service Accounts.", required = true)
    public Path serviceAccountKey;

    public GoogleProvider() {
        super(ProviderEnum.GOOGLE);
    }

    @Override
    public GoogleSecurityKeys securityKeys() throws IOException {
        return new GoogleSecurityKeys()
                .data(FilesHelper.readString(serviceAccountKey));
    }
}
