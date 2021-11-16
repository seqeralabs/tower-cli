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

package io.seqera.tower.cli.commands.global;

import picocli.CommandLine;

public class OrganizationOption {
    
    @CommandLine.Option(names = {"-o", "--organization-id"}, description = "Organization numeric identifier (TOWER_ORGANIZATION_ID).", defaultValue = "${TOWER_ORGANIZATION_ID}")
    public Long organizationId = null;
}
