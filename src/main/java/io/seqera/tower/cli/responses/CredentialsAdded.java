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

package io.seqera.tower.cli.responses;

public class CredentialsAdded extends Response {

    public final String id;
    public final String provider;
    public final String name;
    public final String workspaceRef;

    public CredentialsAdded(String provider, String id, String name, String workspaceRef) {
        this.provider = provider;
        this.id = id;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow New %S credentials '%s (%s)' added at %s workspace|@%n", provider, name, id, workspaceRef));
    }
}
