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

package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;

public class MembersUpdate extends Response {

    public final String user;
    public final String organizationName;
    public final String role;

    public MembersUpdate(String user, String organizationName, String role) {
        this.user = user;
        this.organizationName = organizationName;
        this.role = role;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' updated to role '%s' in organization '%s'|@%n", user, role, organizationName));
    }
}
