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

import io.seqera.tower.ApiException;
import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;

import java.io.IOException;

public abstract class AbstractProvider<T extends SecurityKeys> implements CredentialsProvider {
    private ProviderEnum type;

    public AbstractProvider(ProviderEnum type) {
        this.type = type;
    }

    public String baseUrl() {
        return null;
    }

    public ProviderEnum type() {
        return type;
    }

    public abstract T securityKeys() throws IOException, ApiException;
}
