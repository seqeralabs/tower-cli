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

package io.seqera.tower.cli.commands.organizations;

import picocli.CommandLine;

public class OrganizationRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public OrganizationRef organization;

    public static class OrganizationRef {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Organization unique id.")
        public Long organizationId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Organization name.")
        public String organizationName;
    }
}
