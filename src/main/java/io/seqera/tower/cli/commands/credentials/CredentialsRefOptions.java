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

package io.seqera.tower.cli.commands.credentials;

import picocli.CommandLine;

public class CredentialsRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public CredentialsRef credentialsRef;

    public static class CredentialsRef {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Credentials unique id.")
        public String credentialsId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Credentials name.")
        public String credentialsName;
    }
}
