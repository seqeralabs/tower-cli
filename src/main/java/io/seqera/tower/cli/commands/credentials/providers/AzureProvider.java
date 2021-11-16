/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AzureSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzureProvider extends AbstractProvider<AzureSecurityKeys> {

    @Option(names = {"--batch-key"}, description = "Azure batch account key.", required = true)
    public String batchKey;

    @Option(names = {"--batch-name"}, description = "Azure batch account name.", required = true)
    public String batchName;

    @Option(names = {"--storage-key"}, description = "Azure blob storage account key.", required = true)
    public String storageKey;

    @Option(names = {"--storage-name"}, description = "Azure blob storage account name.", required = true)
    public String storageName;

    public AzureProvider() {
        super(ProviderEnum.AZURE);
    }

    @Override
    public AzureSecurityKeys securityKeys() throws IOException {
        return new AzureSecurityKeys()
                .batchKey(batchKey)
                .batchName(batchName)
                .storageKey(storageKey)
                .storageName(storageName);
    }
}
