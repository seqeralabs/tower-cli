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

package io.seqera.tower.cli.commands.secrets;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class SecretRefOptions {

    @ArgGroup(multiplicity = "1")
    public SecretRef secret;

    public static class SecretRef {

        @Option(names = {"-i", "--id"}, description = "Secret unique id.")
        public Long id;

        @Option(names = {"-n", "--name"}, description = "Secret name.")
        public String name;
    }
}
