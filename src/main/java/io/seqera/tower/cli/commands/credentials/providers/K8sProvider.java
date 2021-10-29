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
import io.seqera.tower.model.K8sSecurityKeys;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class K8sProvider extends AbstractProvider<K8sSecurityKeys> {

    @ArgGroup
    public Keys keys;

    public K8sProvider() {
        super(ProviderEnum.K8S);
    }

    @Override
    public K8sSecurityKeys securityKeys() throws IOException {
        K8sSecurityKeys result = new K8sSecurityKeys();
        if (keys.certs != null) {
            result
                    .certificate(FilesHelper.readString(keys.certs.certificate))
                    .privateKey(FilesHelper.readString(keys.certs.privateKey));
        } else {
            result.token(keys.token);
        }

        return result;
    }

    public static class Keys {

        @Option(names = {"-t", "--token"}, description = "Service account token")
        public String token;

        @ArgGroup(exclusive = false)
        public ClientCerts certs;
    }

    public static class ClientCerts {

        @Option(names = {"-c", "--certificate"}, description = "Client certificate file")
        public Path certificate;

        @Option(names = {"-k", "--private-key"}, description = "Client key file")
        public Path privateKey;
    }
}
