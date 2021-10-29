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

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.GoogleSecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class GoogleProvider extends AbstractProvider<GoogleSecurityKeys> {

    @Option(names = {"-k", "--key"}, description = "JSON file with the service account key", required = true)
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
